package org.ivandgetic.renderer;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;

import org.ivandgetic.AppConfig;
import org.ivandgetic.kube.GLWorld;
import org.ivandgetic.kube.Layer;
import org.ivandgetic.ogles.lib.IBufferFactory;
import org.ivandgetic.ogles.lib.Matrix4f;
import org.ivandgetic.ogles.lib.Ray;
import org.ivandgetic.ogles.lib.Vector3f;
import org.ivandgetic.raypick.PickFactory;
import org.ivandgetic.view.MyGLSurfaceView;

import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class KubeRenderer implements Renderer {
    static int[][] mLayerCWPermutations = {
            {2, 5, 8, 1, 4, 7, 0, 3, 6, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26},// permutation for UP layer
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 20, 23, 26, 19, 22, 25, 18, 21, 24},// permutation for DOWN layer
            {6, 1, 2, 15, 4, 5, 24, 7, 8, 3, 10, 11, 12, 13, 14, 21, 16, 17, 0, 19, 20, 9, 22, 23, 18, 25, 26},// permutation for LEFT layer
            {0, 1, 8, 3, 4, 17, 6, 7, 26, 9, 10, 5, 12, 13, 14, 15, 16, 23, 18, 19, 2, 21, 22, 11, 24, 25, 20},// permutation for RIGHT layer
            {0, 1, 2, 3, 4, 5, 24, 15, 6, 9, 10, 11, 12, 13, 14, 25, 16, 7, 18, 19, 20, 21, 22, 23, 26, 17, 8},// permutation for FRONT layer
            {18, 9, 0, 3, 4, 5, 6, 7, 8, 19, 10, 1, 12, 13, 14, 15, 16, 17, 20, 11, 2, 21, 22, 23, 24, 25, 26},// permutation for BACK layer
            {0, 7, 2, 3, 16, 5, 6, 25, 8, 9, 4, 11, 12, 13, 14, 15, 22, 17, 18, 1, 20, 21, 10, 23, 24, 19, 26},// permutation for MIDDLE layer
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 11, 14, 17, 10, 13, 16, 9, 12, 15, 18, 19, 20, 21, 22, 23, 24, 25, 26},// permutation for EQUATOR layer
            {0, 1, 2, 21, 12, 3, 6, 7, 8, 9, 10, 11, 22, 13, 4, 15, 16, 17, 18, 19, 20, 23, 14, 5, 24, 25, 26} // permutation for SIDE layer
    };
    static int[][] mLayerCCWPermutations = {
            {6, 3, 0, 7, 4, 1, 8, 5, 2, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26},// permutation for UP layer 最上层逆时针旋转90度后布局
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 24, 21, 18, 25, 22, 19, 26, 23, 20},// permutation for DOWN layer 最下层逆时针旋转90度后布局
            {18, 1, 2, 9, 4, 5, 0, 7, 8, 21, 10, 11, 12, 13, 14, 3, 16, 17, 24, 19, 20, 15, 22, 23, 6, 25, 26},// permutation for LEFT layer 左侧
            {0, 1, 20, 3, 4, 11, 6, 7, 2, 9, 10, 23, 12, 13, 14, 15, 16, 5, 18, 19, 26, 21, 22, 17, 24, 25, 8},// permutation for RIGHT layer 右侧
            {0, 1, 2, 3, 4, 5, 8, 17, 26, 9, 10, 11, 12, 13, 14, 7, 16, 25, 18, 19, 20, 21, 22, 23, 6, 15, 24},// permutation for FRONT layer 前面
            {2, 11, 20, 3, 4, 5, 6, 7, 8, 1, 10, 19, 12, 13, 14, 15, 16, 17, 0, 9, 18, 21, 22, 23, 24, 25, 26},// permutation for BACK layer 后面
            {0, 19, 2, 3, 10, 5, 6, 1, 8, 9, 22, 11, 12, 13, 14, 15, 4, 17, 18, 25, 20, 21, 16, 23, 24, 7, 26},// permutation for MIDDLE layer （中间面绕X轴逆时针旋转）
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 15, 12, 9, 16, 13, 10, 17, 14, 11, 18, 19, 20, 21, 22, 23, 24, 25, 26},// permutation for EQUATOR layer (中间绕Y轴逆时针旋转)
            {0, 1, 2, 5, 14, 23, 6, 7, 8, 9, 10, 11, 4, 13, 22, 15, 16, 17, 18, 19, 20, 3, 12, 21, 24, 25, 26}// permutation for SIDE layer	(中间绕Z轴逆时针旋转)
    };
    public float mfAngleX = 0.0f;
    public float mfAngleY = 0.0f;
    public float gesDistance = 0.0f;
    // for random cube movements
    Random mRandom = new Random(System.currentTimeMillis());
    // currently turning layer
    Layer mCurrentLayer = null;
    // current and final angle for current Layer animation
    float mCurrentAngle, mEndAngle;
    // amount to increment angle
    float mAngleIncrement;
    int[] mCurrentLayerPermutation;
    int st;
    private Context mContext;
    private GLWorld mWorld;
    private Vector3f mvEye = new Vector3f(0, 0, 7f), mvCenter = new Vector3f(0, 0, 0), mvUp = new Vector3f(0, 1, 0); // 观察者、中心和上方
    private Vector3f transformedSphereCenter = new Vector3f();
    private Ray transformedRay = new Ray();
    private Matrix4f matInvertModel = new Matrix4f();
    private Vector3f[] mpTriangle = {new Vector3f(), new Vector3f(), new Vector3f()};
    private FloatBuffer mBufPickedTriangle = IBufferFactory.newFloatBuffer(3 * 3);
    private Matrix4f matRot = new Matrix4f();
    private Vector3f point;

    public KubeRenderer(Context context, GLWorld world) {
        mContext = context;
        mWorld = world;
    }

    @Override
    public void onDrawFrame(GL10 gl) {//逐帧渲染
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); // 清除屏幕和深度缓存
        gl.glLoadIdentity(); // 重置当前的模型观察矩阵
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        Matrix4f.gluLookAt(mvEye, mvCenter, mvUp, AppConfig.gMatView);
        gl.glLoadMatrixf(AppConfig.gMatView.asFloatBuffer());
        gl.glPushMatrix();
        {
            rotate(gl);
            drawCube(gl);// 渲染物体
        }
        gl.glPopMatrix();
        updatePick();
        if (AppConfig.daluan) {
            start();
        }
        if (st > 20) {
            AppConfig.daluan = false;
        }
        if (AppConfig.isRotare) {
            startRotate(AppConfig.mian, AppConfig.direction);
        }
        gl.glPushMatrix();
        {
            drawPickedTriangle(gl);// 渲染选中的三角形
        }
    }

    /**
     * 渲染选中的三角形
     */
    private void drawPickedTriangle(GL10 gl) {
        if (!AppConfig.gbTrianglePicked) {
            return;
        }
        // 由于返回的拾取三角形数据是出于模型坐标系中
        // 因此需要经过模型变换，将它们变换到世界坐标系中进行渲染
        // 设置模型变换矩阵
        gl.glMultMatrixf(AppConfig.gMatModel.asFloatBuffer());
        // 设置三角形颜色，alpha为0.7
        gl.glColor4f(1.0f, 0.0f, 0.0f, 0.7f);
        // 开启Blend混合模式
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        // 禁用无关属性，仅仅使用纯色填充
        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glDisable(GL10.GL_TEXTURE_2D);
        // 开始绑定渲染顶点数据
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mBufPickedTriangle);
        // 提交渲染
        gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);
        // 重置相关属性
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDisable(GL10.GL_BLEND);
    }

    public void startRotate(int mian, Boolean direction) {
        if (mCurrentLayer == null) {
            mCurrentLayer = MyGLSurfaceView.mLayers[mian];
            if (direction) {
                mCurrentLayerPermutation = mLayerCCWPermutations[mian];
            } else {
                mCurrentLayerPermutation = mLayerCWPermutations[mian];
            }
            mCurrentLayer.startAnimation();
            mCurrentAngle = 0;
            if (direction) {
                mAngleIncrement = (float) Math.PI / 25;
                mEndAngle = mCurrentAngle + ((float) Math.PI) / 2f;
            } else {
                mAngleIncrement = -(float) Math.PI / 25;
                mEndAngle = mCurrentAngle - ((float) Math.PI) / 2f;
            }
        }
        mCurrentAngle += mAngleIncrement;
        if ((mAngleIncrement > 0f && mCurrentAngle >= mEndAngle) || (mAngleIncrement < 0f && mCurrentAngle <= mEndAngle)) {
            mCurrentLayer.setAngle(mEndAngle);
            mCurrentLayer.endAnimation();
            mCurrentLayer = null;
            int[] newPermutation = new int[27];
            for (int i = 0; i < 27; i++) {
                newPermutation[i] = MyGLSurfaceView.mPermutation[mCurrentLayerPermutation[i]];
            }
            MyGLSurfaceView.mPermutation = newPermutation;
            MyGLSurfaceView.updateLayers();
            AppConfig.isRotare = false;
        } else {
            mCurrentLayer.setAngle(mCurrentAngle);
        }
    }

    public void start() {
        if (mCurrentLayer == null) {
            boolean direction = mRandom.nextBoolean();
            int layerID = mRandom.nextInt(9);
            mCurrentLayer = MyGLSurfaceView.mLayers[layerID];
            if (direction) {
                mCurrentLayerPermutation = mLayerCCWPermutations[layerID];
            } else {
                mCurrentLayerPermutation = mLayerCWPermutations[layerID];
            }
            mCurrentLayer.startAnimation();
            mCurrentAngle = 0;

            if (direction) {
                mAngleIncrement = (float) Math.PI / 25;
                mEndAngle = mCurrentAngle + ((float) Math.PI) / 2f;
            } else {
                mAngleIncrement = -(float) Math.PI / 25;
                mEndAngle = mCurrentAngle - ((float) Math.PI) / 2f;
            }
        }
        mCurrentAngle += mAngleIncrement;
        if ((mAngleIncrement > 0f && mCurrentAngle >= mEndAngle) || (mAngleIncrement < 0f && mCurrentAngle <= mEndAngle)) {
            mCurrentLayer.setAngle(mEndAngle);
            mCurrentLayer.endAnimation();
            mCurrentLayer = null;
            int[] newPermutation = new int[27];
            for (int i = 0; i < 27; i++) {
                newPermutation[i] = MyGLSurfaceView.mPermutation[mCurrentLayerPermutation[i]];
            }
            MyGLSurfaceView.mPermutation = newPermutation;
            MyGLSurfaceView.updateLayers();
            st++;
        } else {
            mCurrentLayer.setAngle(mCurrentAngle);
        }
    }

    private void updatePick() {//更新拾取事件
        if (!AppConfig.gbNeedPick) {
            return;
        }
        AppConfig.gbNeedPick = false; // 更新最新的拾取射线
        PickFactory.update(AppConfig.gScreenX, AppConfig.gScreenY); // 获得最新的拾取射线
        Ray ray = PickFactory.getPickRay();  // 首先把模型的绑定球通过模型矩阵，由模型局部空间变换到世界空间
        AppConfig.gMatModel.transform(mWorld.getSphereCenter(), transformedSphereCenter);
        mWorld.surface = -1;    // 触碰的立方体面的标记为无
        // 首先检测拾取射线是否与模型绑定球发生相交
        // 这个检测很快，可以快速排除不必要的精确相交检测
        if (ray.intersectSphere(transformedSphereCenter, mWorld.getSphereRadius())) {
            // 如果射线与绑定球发生相交，那么就需要进行精确的三角面级别的相交检测
            // 由于我们的模型渲染数据，均是在模型局部坐标系中
            // 而拾取射线是在世界坐标系中
            // 因此需要把射线转换到模型坐标系中
            // 这里首先计算模型矩阵的逆矩阵
            matInvertModel.set(AppConfig.gMatModel);
            matInvertModel.invert();
            // 把射线变换到模型坐标系中，把结果存储到transformedRay中
            ray.transform(matInvertModel, transformedRay);
            // 将射线与模型做精确相交检测
            if (mWorld.intersect(transformedRay, mpTriangle)) {
                // 如果找到了相交的最近的三角形
                AppConfig.gbTrianglePicked = true;
                // 触碰了哪一个面
                AppConfig.canRotate = true;
                System.out.println(mWorld.surface + "面");
                AppConfig.currentNumber = mWorld.surface;
                // 填充数据到被选取三角形的渲染缓存中
                mBufPickedTriangle.clear();
                for (int i = 0; i < 3; i++) {
                    IBufferFactory.fillBuffer(mBufPickedTriangle, mpTriangle[i]);
                }
                mBufPickedTriangle.position(0);
            }
        } else {
            AppConfig.canRotate = false;
            AppConfig.gbTrianglePicked = false;
            AppConfig.startRotate = true;
        }
    }

    private void drawCube(GL10 gl) {
        gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        //gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glColor4f(0.7f, 0.7f, 0.7f, 1.0f);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        mWorld.draw(gl);//画魔方
        if (AppConfig.showCoordinateSystem) {
            drawCoordinateSystem(gl);//画坐标系
        }
    }

    private void rotate(GL10 gl) {//旋转操作
        matRot.setIdentity();
        point = new Vector3f(mfAngleX, mfAngleY, 0); // 世界坐标系的向量点
        try {
            // 转换到模型内部的点，先要求逆
            matInvertModel.set(AppConfig.gMatModel);
            matInvertModel.invert();
            matInvertModel.transform(point, point);
            float d = Vector3f.distance(new Vector3f(), point);
            // 再减少误差
            if (Math.abs(d - gesDistance) <= 1E-4) {
                // 绕这个单位向量旋转（由于误差可能会产生缩放而使得模型消失不见）
                matRot.glRotatef((float) (gesDistance * Math.PI / 180), point.x / d, point.y / d, point.z / d);
                // 旋转后在原基础上再转
                if (0 != gesDistance) {
                    AppConfig.gMatModel.mul(matRot);
                }
            }
        } catch (Exception e) {  // 由于四舍五入求逆矩阵失败
        }
        gesDistance = 0;
        gl.glMultMatrixf(AppConfig.gMatModel.asFloatBuffer());
    }

    private void drawCoordinateSystem(GL10 gl) {//渲染坐标系
        gl.glDisable(GL10.GL_DEPTH_TEST); // 暂时禁用深度测试
        gl.glLineWidth(2.0f);  // 设置点和线的宽度
        // 仅仅启用顶点数据
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        FloatBuffer fb = IBufferFactory.newFloatBuffer(3 * 2);
        fb.put(new float[]{0, 0, 0, 1.5f, 0, 0});
        fb.position(0);
        // 渲染X轴
        gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);// 设置红色
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb);
        // 提交渲染
        gl.glDrawArrays(GL10.GL_LINES, 0, 2);
        fb.clear();
        fb.put(new float[]{0, 0, 0, 0, 1.5f, 0});
        fb.position(0);
        // 渲染Y轴
        gl.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);// 设置绿色
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb);
        // 提交渲染
        gl.glDrawArrays(GL10.GL_LINES, 0, 2);
        fb.clear();
        fb.put(new float[]{0, 0, 0, 0, 0, 1.5f});
        fb.position(0);
        // 渲染Z轴
        gl.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);// 设置蓝色
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb);
        gl.glDrawArrays(GL10.GL_LINES, 0, 2);  // 提交渲染
        // 重置
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glLineWidth(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
    }

    /**
     * 创建绘图表面时调用
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {  // 全局性设置
        gl.glEnable(GL10.GL_DITHER);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        gl.glClearColor(0.5f, 0.5f, 0.5f, 1);// 设置清屏背景颜色
        gl.glShadeModel(GL10.GL_SMOOTH);// 设置着色模型为平滑着色
        // 启用背面剪裁
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glCullFace(GL10.GL_BACK);
        // 启用深度测试
        gl.glEnable(GL10.GL_DEPTH_TEST);
        // 禁用光照和混合
        gl.glDisable(GL10.GL_LIGHTING);
        gl.glDisable(GL10.GL_BLEND);
        AppConfig.gMatModel.setIdentity();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {//当绘图表面尺寸发生改变时调用
        gl.glViewport(0, 0, width, height);// 设置视口
        AppConfig.gpViewport[0] = 0;
        AppConfig.gpViewport[1] = 0;
        AppConfig.gpViewport[2] = width;
        AppConfig.gpViewport[3] = height;
        // 设置投影矩阵
        float ratio = (float) width / height;// 屏幕宽高比
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        Matrix4f.gluPersective(45.0f, ratio, 1, 10, AppConfig.gMatProject);
        gl.glLoadMatrixf(AppConfig.gMatProject.asFloatBuffer());
        AppConfig.gMatProject.fillFloatArray(AppConfig.gpMatrixProjectArray);
        // 每次修改完GL_PROJECTION后，最好将当前矩阵模型设置回GL_MODELVIEW
        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }

}
