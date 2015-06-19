/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ivandgetic.kube;

import org.ivandgetic.ogles.lib.Ray;
import org.ivandgetic.ogles.lib.Vector3f;
import org.ivandgetic.ogles.lib.Vector4f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

public class GLWorld {
    public Vector3f getSphereCenter() {
        return new Vector3f(0, 0, 0);
    }

    private byte[] indices = new byte[]{0, 1, 3, 2, 4, 5, 7, 6, 8, 9, 11, 10, 12, 13, 15, 14, 16, 17, 19, 18, 20, 21, 23, 22};    // 三角形描述顺序

    public float getSphereRadius() {
        return 1.732051f;// 返回立方体外切圆的半径（√3）
    }

    public int surface = -1;// 触碰的立方体某一面的标记（0-5）

    public boolean intersect(Ray ray, Vector3f[] trianglePosOut) {
        boolean bFound = false;
        // 存储着射线原点与三角形相交点的距离
        // 我们最后仅仅保留距离最近的那一个
        float closeDis = 0.0f;
        Vector3f v0, v1, v2;
        for (int i = 0; i < 6; i++) {// 立方体6个面
            // 每个面两个三角形
            for (int j = 0; j < 2; j++) {
                if (0 == j) {
                    v0 = getVector3f(indices[i * 4 + j]);
                    v1 = getVector3f(indices[i * 4 + j + 1]);
                    v2 = getVector3f(indices[i * 4 + j + 2]);
                } else {
                    // 第二个三角形时，换下顺序，不然会渲染到立方体内部
                    v0 = getVector3f(indices[i * 4 + j]);
                    v1 = getVector3f(indices[i * 4 + j + 2]);
                    v2 = getVector3f(indices[i * 4 + j + 1]);
                }
                // 进行射线和三角行的碰撞检测
                if (ray.intersectTriangle(v0, v1, v2, location)) {
                    // 如果发生了相交
                    if (!bFound) {
                        // 如果是初次检测到，需要存储射线原点与三角形交点的距离值
                        bFound = true;
                        closeDis = location.w;
                        trianglePosOut[0].set(v0);
                        trianglePosOut[1].set(v1);
                        trianglePosOut[2].set(v2);
                        surface = i;
                    } else {
                        // 如果之前已经检测到相交事件，则需要把新相交点与之前的相交数据相比较
                        // 最终保留离射线原点更近的
                        if (closeDis > location.w) {
                            closeDis = location.w;
                            trianglePosOut[0].set(v0);
                            trianglePosOut[1].set(v1);
                            trianglePosOut[2].set(v2);
                            surface = i;
                        }
                    }
                }
            }
        }
        return bFound;
    }

    private static Vector4f location = new Vector4f();
    private float one = 1.0f;
    // 立方体顶点坐标
    private float[] vertices = new float[]{-one, -one, one, one, -one, one,
            one, one, one, -one, one, one, -one, -one, -one, -one, one, -one,
            one, one, -one, one, -one, -one, -one, one, -one, -one, one, one,
            one, one, one, one, one, -one, -one, -one, -one, one, -one, -one,
            one, -one, one, -one, -one, one, one, -one, -one, one, one, -one,
            one, one, one, one, -one, one, -one, -one, -one, -one, -one, one,
            -one, one, one, -one, one, -one};

    private Vector3f getVector3f(int start) {
        return new Vector3f(vertices[3 * start], vertices[3 * start + 1],
                vertices[3 * start + 2]);
    }


    /////////////////////////
    public void addShape(GLShape shape) {
        mShapeList.add(shape);
        mIndexCount += shape.getIndexCount();
    }

    public void generate() {
        ByteBuffer bb = ByteBuffer.allocateDirect(mVertexList.size() * 4 * 4);
        bb.order(ByteOrder.nativeOrder());
        mColorBuffer = bb.asIntBuffer();

        bb = ByteBuffer.allocateDirect(mVertexList.size() * 4 * 3);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asIntBuffer();

        bb = ByteBuffer.allocateDirect(mIndexCount * 2);
        bb.order(ByteOrder.nativeOrder());
        mIndexBuffer = bb.asShortBuffer();

        Iterator<GLVertex> iter2 = mVertexList.iterator();
        while (iter2.hasNext()) {
            GLVertex vertex = iter2.next();
            vertex.put(mVertexBuffer, mColorBuffer);
        }

        Iterator<GLShape> iter3 = mShapeList.iterator();
        while (iter3.hasNext()) {
            GLShape shape = iter3.next();
            shape.putIndices(mIndexBuffer);
        }
    }

    public GLVertex addVertex(float x, float y, float z) {
        GLVertex vertex = new GLVertex(x, y, z, mVertexList.size());
        mVertexList.add(vertex);
        return vertex;
    }

    public void transformVertex(GLVertex vertex, M4 transform) {
        vertex.update(mVertexBuffer, transform);
    }

    int count = 0;

    public void draw(GL10 gl) {
        mColorBuffer.position(0);
        mVertexBuffer.position(0);
        mIndexBuffer.position(0);

        gl.glFrontFace(GL10.GL_CW);
        gl.glShadeModel(GL10.GL_FLAT);
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
        gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, mIndexCount, GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
        count++;
    }

    static public float toFloat(int x) {
        return x / 65536.0f;
    }

    private ArrayList<GLShape> mShapeList = new ArrayList<GLShape>();
    private ArrayList<GLVertex> mVertexList = new ArrayList<GLVertex>();

    private int mIndexCount = 0;

    private IntBuffer mVertexBuffer;
    private IntBuffer mColorBuffer;
    private ShortBuffer mIndexBuffer;
}
