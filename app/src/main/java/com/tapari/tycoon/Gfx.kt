package com.tapari.tycoon

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/** Zero-allocation 4x4 column-major matrix helpers. */
object Mat {
    fun perspective(m: FloatArray, fovyDeg: Float, aspect: Float, near: Float, far: Float) {
        val f = 1f / tan(Math.toRadians(fovyDeg.toDouble() / 2.0)).toFloat()
        for (i in 0 until 16) m[i] = 0f
        m[0] = f / aspect
        m[5] = f
        m[10] = (far + near) / (near - far)
        m[11] = -1f
        m[14] = 2f * far * near / (near - far)
    }

    fun lookAt(m: FloatArray, ex: Float, ey: Float, ez: Float, cx: Float, cy: Float, cz: Float) {
        var fx = cx - ex
        var fy = cy - ey
        var fz = cz - ez
        var l = sqrt(fx * fx + fy * fy + fz * fz)
        fx /= l; fy /= l; fz /= l
        // s = f x up(0,1,0)
        var sx = fy * 0f - fz * 1f
        var sy = fz * 0f - fx * 0f
        var sz = fx * 1f - fy * 0f
        l = sqrt(sx * sx + sy * sy + sz * sz)
        sx /= l; sy /= l; sz /= l
        val ux = sy * fz - sz * fy
        val uy = sz * fx - sx * fz
        val uz = sx * fy - sy * fx
        m[0] = sx; m[1] = ux; m[2] = -fx; m[3] = 0f
        m[4] = sy; m[5] = uy; m[6] = -fy; m[7] = 0f
        m[8] = sz; m[9] = uz; m[10] = -fz; m[11] = 0f
        m[12] = -(sx * ex + sy * ey + sz * ez)
        m[13] = -(ux * ex + uy * ey + uz * ez)
        m[14] = fx * ex + fy * ey + fz * ez
        m[15] = 1f
    }

    /** out = a * b (out must not alias a or b). */
    fun mul(out: FloatArray, a: FloatArray, b: FloatArray) {
        for (c in 0 until 4) {
            for (r in 0 until 4) {
                var s = 0f
                for (k in 0 until 4) s += a[k * 4 + r] * b[c * 4 + k]
                out[c * 4 + r] = s
            }
        }
    }
}

object Gl {
    private fun compile(type: Int, src: String): Int {
        val s = GLES30.glCreateShader(type)
        GLES30.glShaderSource(s, src)
        GLES30.glCompileShader(s)
        val ok = IntArray(1)
        GLES30.glGetShaderiv(s, GLES30.GL_COMPILE_STATUS, ok, 0)
        if (ok[0] == 0) {
            val log = GLES30.glGetShaderInfoLog(s)
            GLES30.glDeleteShader(s)
            throw RuntimeException("Shader compile failed: $log")
        }
        return s
    }

    fun program(vs: String, fs: String): Int {
        val v = compile(GLES30.GL_VERTEX_SHADER, vs)
        val f = compile(GLES30.GL_FRAGMENT_SHADER, fs)
        val p = GLES30.glCreateProgram()
        GLES30.glAttachShader(p, v)
        GLES30.glAttachShader(p, f)
        GLES30.glLinkProgram(p)
        val ok = IntArray(1)
        GLES30.glGetProgramiv(p, GLES30.GL_LINK_STATUS, ok, 0)
        if (ok[0] == 0) {
            val log = GLES30.glGetProgramInfoLog(p)
            throw RuntimeException("Program link failed: $log")
        }
        GLES30.glDeleteShader(v)
        GLES30.glDeleteShader(f)
        return p
    }

    const val LIT_VS = """#version 300 es
uniform mat4 uVP;
uniform vec3 uSunDir;
uniform vec3 uSunCol;
uniform vec3 uSky;
uniform vec3 uGround;
layout(location=0) in vec3 aPos;
layout(location=1) in vec3 aNrm;
layout(location=2) in vec4 aCol;
out vec3 vCol;
void main() {
  vec3 n = normalize(aNrm);
  float d = max(dot(n, uSunDir), 0.0);
  vec3 amb = mix(uGround, uSky, n.y * 0.5 + 0.5);
  float ao = mix(0.86, 1.0, smoothstep(0.0, 0.8, aPos.y));
  vec3 lit = aCol.rgb * (amb + uSunCol * d) * ao;
  vCol = mix(aCol.rgb, lit, aCol.a);
  gl_Position = uVP * vec4(aPos, 1.0);
}
"""
    const val LIT_FS = """#version 300 es
precision mediump float;
in vec3 vCol;
out vec4 o;
void main() {
  o = vec4(vCol, 1.0);
}
"""
    const val SPR_VS = """#version 300 es
uniform mat4 uVP;
layout(location=0) in vec3 aPos;
layout(location=1) in vec2 aUV;
layout(location=2) in vec4 aCol;
out vec2 vUV;
out vec4 vCol;
void main() {
  vUV = aUV;
  vCol = aCol;
  gl_Position = uVP * vec4(aPos, 1.0);
}
"""
    const val SPR_FS = """#version 300 es
precision mediump float;
uniform sampler2D uTex;
in vec2 vUV;
in vec4 vCol;
out vec4 o;
void main() {
  vec4 t = texture(uTex, vUV);
  o = vec4(t.rgb * vCol.rgb * vCol.a, t.a * vCol.a);
}
"""
}

class Shape(val pos: FloatArray, val nrm: FloatArray, val idx: IntArray) {
    val vc = pos.size / 3
    val ic = idx.size
}

object Shapes {
    private class B {
        val p = ArrayList<Float>()
        val n = ArrayList<Float>()
        val i = ArrayList<Int>()
        fun v(x: Float, y: Float, z: Float, nx: Float, ny: Float, nz: Float): Int {
            p.add(x); p.add(y); p.add(z)
            n.add(nx); n.add(ny); n.add(nz)
            return p.size / 3 - 1
        }
        fun tri(a: Int, b: Int, c: Int) { i.add(a); i.add(b); i.add(c) }
        fun build(): Shape = Shape(p.toFloatArray(), n.toFloatArray(), i.toIntArray())
    }

    val CUBE: Shape = cube()
    val PLANE: Shape = plane()
    val CYL6: Shape = cylinder(6)
    val CYL8: Shape = cylinder(8)
    val CONE8: Shape = cone(8)
    val CONE5: Shape = cone(5)
    val SPHERE: Shape = sphere(8, 5)
    val SPHERE_LO: Shape = sphere(6, 4)

    private fun cube(): Shape {
        val b = B()
        for (axis in 0 until 3) {
            for (sgn in intArrayOf(-1, 1)) {
                val u = (axis + 1) % 3
                val v = (axis + 2) % 3
                val base = b.p.size / 3
                for (c in 0 until 4) {
                    val cu = if (c == 1 || c == 2) 0.5f else -0.5f
                    val cv = if (c >= 2) 0.5f else -0.5f
                    val pos = FloatArray(3)
                    val nn = FloatArray(3)
                    pos[axis] = sgn * 0.5f
                    pos[u] = cu
                    pos[v] = cv
                    nn[axis] = sgn.toFloat()
                    b.v(pos[0], pos[1], pos[2], nn[0], nn[1], nn[2])
                }
                b.tri(base, base + 1, base + 2)
                b.tri(base, base + 2, base + 3)
            }
        }
        return b.build()
    }

    private fun plane(): Shape {
        val b = B()
        val a = b.v(-0.5f, 0f, -0.5f, 0f, 1f, 0f)
        b.v(0.5f, 0f, -0.5f, 0f, 1f, 0f)
        b.v(0.5f, 0f, 0.5f, 0f, 1f, 0f)
        b.v(-0.5f, 0f, 0.5f, 0f, 1f, 0f)
        b.tri(a, a + 1, a + 2)
        b.tri(a, a + 2, a + 3)
        return b.build()
    }

    private fun cylinder(n: Int): Shape {
        val b = B()
        val step = (2.0 * Math.PI / n).toFloat()
        for (k in 0 until n) {
            val a0 = k * step
            val a1 = (k + 1) * step
            val am = (a0 + a1) / 2f
            val nx = cos(am)
            val nz = sin(am)
            val i0 = b.v(cos(a0), -0.5f, sin(a0), nx, 0f, nz)
            val i1 = b.v(cos(a1), -0.5f, sin(a1), nx, 0f, nz)
            val i2 = b.v(cos(a1), 0.5f, sin(a1), nx, 0f, nz)
            val i3 = b.v(cos(a0), 0.5f, sin(a0), nx, 0f, nz)
            b.tri(i0, i1, i2)
            b.tri(i0, i2, i3)
        }
        val c = b.v(0f, 0.5f, 0f, 0f, 1f, 0f)
        val r0 = b.p.size / 3
        for (k in 0 until n) {
            val a = k * step
            b.v(cos(a), 0.5f, sin(a), 0f, 1f, 0f)
        }
        for (k in 0 until n) {
            b.tri(c, r0 + k, r0 + (k + 1) % n)
        }
        return b.build()
    }

    private fun cone(n: Int): Shape {
        val b = B()
        val step = (2.0 * Math.PI / n).toFloat()
        for (k in 0 until n) {
            val a0 = k * step
            val a1 = (k + 1) * step
            val am = (a0 + a1) / 2f
            val nx = cos(am)
            val nz = sin(am)
            val t = b.v(0f, 0.5f, 0f, nx, 1f, nz)
            val i0 = b.v(cos(a0), -0.5f, sin(a0), nx, 1f, nz)
            val i1 = b.v(cos(a1), -0.5f, sin(a1), nx, 1f, nz)
            b.tri(t, i0, i1)
        }
        val c = b.v(0f, -0.5f, 0f, 0f, -1f, 0f)
        val r0 = b.p.size / 3
        for (k in 0 until n) b.v(cos(k * step), -0.5f, sin(k * step), 0f, -1f, 0f)
        for (k in 0 until n) b.tri(c, r0 + (k + 1) % n, r0 + k)
        return b.build()
    }

    private fun sphere(segs: Int, rings: Int): Shape {
        val b = B()
        val sp = Array(rings + 1) { FloatArray(3 * (segs + 1)) }
        for (k in 0..rings) {
            val phi = (-Math.PI / 2.0 + Math.PI * k / rings).toFloat()
            for (i in 0..segs) {
                val th = (2.0 * Math.PI * i / segs).toFloat()
                sp[k][i * 3] = cos(phi) * cos(th)
                sp[k][i * 3 + 1] = sin(phi)
                sp[k][i * 3 + 2] = cos(phi) * sin(th)
            }
        }
        fun addTri(k0: Int, i0: Int, k1: Int, i1: Int, k2: Int, i2: Int) {
            val ax = sp[k0][i0 * 3]; val ay = sp[k0][i0 * 3 + 1]; val az = sp[k0][i0 * 3 + 2]
            val bx = sp[k1][i1 * 3]; val by = sp[k1][i1 * 3 + 1]; val bz = sp[k1][i1 * 3 + 2]
            val cx = sp[k2][i2 * 3]; val cy = sp[k2][i2 * 3 + 1]; val cz = sp[k2][i2 * 3 + 2]
            var nx = ax + bx + cx
            var ny = ay + by + cy
            var nz = az + bz + cz
            val l = sqrt(nx * nx + ny * ny + nz * nz)
            nx /= l; ny /= l; nz /= l
            val a = b.v(ax, ay, az, nx, ny, nz)
            val bb = b.v(bx, by, bz, nx, ny, nz)
            val c = b.v(cx, cy, cz, nx, ny, nz)
            b.tri(a, bb, c)
        }
        for (k in 0 until rings) {
            for (i in 0 until segs) {
                if (k != rings - 1) addTri(k, i, k + 1, i, k + 1, i + 1)
                if (k != 0) addTri(k, i, k + 1, i + 1, k, i + 1)
            }
        }
        return b.build()
    }
}

/** Dynamic batch of lit low-poly geometry written fresh every frame (no per-frame allocation). */
class LitBatch(private val maxV: Int, private val maxI: Int) {
    private val vtx = IntArray(maxV * 7)
    private val idx = IntArray(maxI)
    var vn = 0
    var inn = 0
    fun rawVerts(): IntArray = vtx
    fun rawIdx(): IntArray = idx
    private val m = FloatArray(16 * 16)
    private var top = 0
    private val vbuf: IntBuffer = ByteBuffer.allocateDirect(maxV * 7 * 4).order(ByteOrder.nativeOrder()).asIntBuffer()
    private val ibuf: IntBuffer = ByteBuffer.allocateDirect(maxI * 4).order(ByteOrder.nativeOrder()).asIntBuffer()
    private var vao = 0
    private var vbo = 0
    private var ibo = 0
    private val ids = IntArray(2)

    fun initGl() {
        GLES30.glGenBuffers(2, ids, 0)
        vbo = ids[0]
        ibo = ids[1]
        val va = IntArray(1)
        GLES30.glGenVertexArrays(1, va, 0)
        vao = va[0]
        GLES30.glBindVertexArray(vao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, maxV * 28, null, GLES30.GL_STREAM_DRAW)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, maxI * 4, null, GLES30.GL_STREAM_DRAW)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 28, 0)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 28, 12)
        GLES30.glEnableVertexAttribArray(2)
        GLES30.glVertexAttribPointer(2, 4, GLES30.GL_UNSIGNED_BYTE, true, 28, 24)
        GLES30.glBindVertexArray(0)
    }

    fun reset() {
        vn = 0
        inn = 0
        loadId()
    }

    fun flush() {
        if (inn == 0) return
        vbuf.clear()
        vbuf.put(vtx, 0, vn * 7)
        vbuf.flip()
        ibuf.clear()
        ibuf.put(idx, 0, inn)
        ibuf.flip()
        GLES30.glBindVertexArray(vao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, vn * 28, vbuf)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GLES30.glBufferSubData(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0, inn * 4, ibuf)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, inn, GLES30.GL_UNSIGNED_INT, 0)
        GLES30.glBindVertexArray(0)
    }

    // ---- matrix stack ----
    fun loadId() {
        top = 0
        for (i in 0 until 16) m[i] = 0f
        m[0] = 1f; m[5] = 1f; m[10] = 1f; m[15] = 1f
    }

    fun push() {
        System.arraycopy(m, top * 16, m, (top + 1) * 16, 16)
        top++
    }

    fun pop() {
        top--
    }

    fun translate(x: Float, y: Float, z: Float) {
        val b = top * 16
        m[b + 12] += m[b] * x + m[b + 4] * y + m[b + 8] * z
        m[b + 13] += m[b + 1] * x + m[b + 5] * y + m[b + 9] * z
        m[b + 14] += m[b + 2] * x + m[b + 6] * y + m[b + 10] * z
    }

    fun rotY(a: Float) {
        val c = cos(a)
        val s = sin(a)
        val b = top * 16
        for (r in 0 until 3) {
            val c0 = m[b + r]
            val c2 = m[b + 8 + r]
            m[b + r] = c0 * c - c2 * s
            m[b + 8 + r] = c0 * s + c2 * c
        }
    }

    fun rotX(a: Float) {
        val c = cos(a)
        val s = sin(a)
        val b = top * 16
        for (r in 0 until 3) {
            val c1 = m[b + 4 + r]
            val c2 = m[b + 8 + r]
            m[b + 4 + r] = c1 * c + c2 * s
            m[b + 8 + r] = -c1 * s + c2 * c
        }
    }

    fun rotZ(a: Float) {
        val c = cos(a)
        val s = sin(a)
        val b = top * 16
        for (r in 0 until 3) {
            val c0 = m[b + r]
            val c1 = m[b + 4 + r]
            m[b + r] = c0 * c + c1 * s
            m[b + 4 + r] = -c0 * s + c1 * c
        }
    }

    fun scale(k: Float) {
        val b = top * 16
        for (i in 0 until 12) m[b + i] *= k
    }

    // ---- primitives ----
    fun shape(s: Shape, ox: Float, oy: Float, oz: Float, sx: Float, sy: Float, sz: Float, color: Int) {
        if (vn + s.vc > maxV || inn + s.ic > maxI) return
        val b = top * 16
        val m0 = m[b]; val m1 = m[b + 1]; val m2 = m[b + 2]
        val m4 = m[b + 4]; val m5 = m[b + 5]; val m6 = m[b + 6]
        val m8 = m[b + 8]; val m9 = m[b + 9]; val m10 = m[b + 10]
        val tx = m[b + 12]; val ty = m[b + 13]; val tz = m[b + 14]
        var o = vn * 7
        val p = s.pos
        val nr = s.nrm
        for (i in 0 until s.vc) {
            val j = i * 3
            val lx = p[j] * sx + ox
            val ly = p[j + 1] * sy + oy
            val lz = p[j + 2] * sz + oz
            vtx[o] = (m0 * lx + m4 * ly + m8 * lz + tx).toRawBits()
            vtx[o + 1] = (m1 * lx + m5 * ly + m9 * lz + ty).toRawBits()
            vtx[o + 2] = (m2 * lx + m6 * ly + m10 * lz + tz).toRawBits()
            val nx = nr[j]; val ny = nr[j + 1]; val nz = nr[j + 2]
            vtx[o + 3] = (m0 * nx + m4 * ny + m8 * nz).toRawBits()
            vtx[o + 4] = (m1 * nx + m5 * ny + m9 * nz).toRawBits()
            vtx[o + 5] = (m2 * nx + m6 * ny + m10 * nz).toRawBits()
            vtx[o + 6] = color
            o += 7
        }
        val si = s.idx
        val base = vn
        for (i in 0 until s.ic) idx[inn + i] = si[i] + base
        vn += s.vc
        inn += s.ic
    }

    fun box(ox: Float, oy: Float, oz: Float, sx: Float, sy: Float, sz: Float, c: Int) =
        shape(Shapes.CUBE, ox, oy, oz, sx, sy, sz, c)

    fun cyl(ox: Float, oy: Float, oz: Float, r: Float, h: Float, c: Int) =
        shape(Shapes.CYL8, ox, oy, oz, r, h, r, c)

    fun cyl6(ox: Float, oy: Float, oz: Float, r: Float, h: Float, c: Int) =
        shape(Shapes.CYL6, ox, oy, oz, r, h, r, c)

    fun cone(ox: Float, oy: Float, oz: Float, r: Float, h: Float, c: Int) =
        shape(Shapes.CONE8, ox, oy, oz, r, h, r, c)

    fun sph(ox: Float, oy: Float, oz: Float, rx: Float, ry: Float, rz: Float, c: Int) =
        shape(Shapes.SPHERE, ox, oy, oz, rx, ry, rz, c)

    fun sphLo(ox: Float, oy: Float, oz: Float, rx: Float, ry: Float, rz: Float, c: Int) =
        shape(Shapes.SPHERE_LO, ox, oy, oz, rx, ry, rz, c)

    fun plane(ox: Float, oy: Float, oz: Float, sx: Float, sz: Float, c: Int) =
        shape(Shapes.PLANE, ox, oy, oz, sx, 1f, sz, c)
}

/** Dynamic batch of textured quads. */
class SpriteBatch(private val maxQ: Int) {
    private val v = IntArray(maxQ * 4 * 6)
    var qn = 0
    fun rawVerts(): IntArray = v
    private val vbuf: IntBuffer = ByteBuffer.allocateDirect(maxQ * 4 * 6 * 4).order(ByteOrder.nativeOrder()).asIntBuffer()
    private var vao = 0
    private var vbo = 0
    private var ibo = 0
    private val ids = IntArray(2)

    fun initGl() {
        GLES30.glGenBuffers(2, ids, 0)
        vbo = ids[0]
        ibo = ids[1]
        val va = IntArray(1)
        GLES30.glGenVertexArrays(1, va, 0)
        vao = va[0]
        GLES30.glBindVertexArray(vao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, maxQ * 4 * 24, null, GLES30.GL_STREAM_DRAW)
        // static index buffer
        val ib = ByteBuffer.allocateDirect(maxQ * 6 * 4).order(ByteOrder.nativeOrder()).asIntBuffer()
        for (q in 0 until maxQ) {
            val b = q * 4
            ib.put(b); ib.put(b + 1); ib.put(b + 2)
            ib.put(b); ib.put(b + 2); ib.put(b + 3)
        }
        ib.flip()
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, ibo)
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, maxQ * 6 * 4, ib, GLES30.GL_STATIC_DRAW)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 24, 0)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 24, 12)
        GLES30.glEnableVertexAttribArray(2)
        GLES30.glVertexAttribPointer(2, 4, GLES30.GL_UNSIGNED_BYTE, true, 24, 20)
        GLES30.glBindVertexArray(0)
    }

    fun reset() {
        qn = 0
    }

    private fun put(o: Int, x: Float, y: Float, z: Float, u: Float, w: Float, col: Int) {
        v[o] = x.toRawBits()
        v[o + 1] = y.toRawBits()
        v[o + 2] = z.toRawBits()
        v[o + 3] = u.toRawBits()
        v[o + 4] = w.toRawBits()
        v[o + 5] = col
    }

    /** Quad centred at (cx,cy,cz) spanned by right (r) and up (u) vectors with half extents. */
    fun quad(
        cx: Float, cy: Float, cz: Float,
        rx: Float, ry: Float, rz: Float,
        ux: Float, uy: Float, uz: Float,
        hw: Float, hh: Float, id: Int, col: Int
    ) {
        if (qn >= maxQ) return
        val o = qn * 24
        val ax = rx * hw; val ay = ry * hw; val az = rz * hw
        val bx = ux * hh; val by = uy * hh; val bz = uz * hh
        val u0 = Atlas.u0[id]; val v0 = Atlas.v0[id]
        val u1 = Atlas.u1[id]; val v1 = Atlas.v1[id]
        // top-left, top-right, bottom-right, bottom-left
        put(o, cx - ax + bx, cy - ay + by, cz - az + bz, u0, v0, col)
        put(o + 6, cx + ax + bx, cy + ay + by, cz + az + bz, u1, v0, col)
        put(o + 12, cx + ax - bx, cy + ay - by, cz + az - bz, u1, v1, col)
        put(o + 18, cx - ax - bx, cy - ay - by, cz - az - bz, u0, v1, col)
        qn++
    }

    fun flush() {
        if (qn == 0) return
        vbuf.clear()
        vbuf.put(v, 0, qn * 24)
        vbuf.flip()
        GLES30.glBindVertexArray(vao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, qn * 96, vbuf)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, qn * 6, GLES30.GL_UNSIGNED_INT, 0)
        GLES30.glBindVertexArray(0)
    }
}
