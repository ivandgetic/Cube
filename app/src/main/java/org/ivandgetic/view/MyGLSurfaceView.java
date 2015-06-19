package org.ivandgetic.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.ivandgetic.AppConfig;
import org.ivandgetic.cube.R;
import org.ivandgetic.kube.Cube;
import org.ivandgetic.kube.GLColor;
import org.ivandgetic.kube.GLShape;
import org.ivandgetic.kube.GLWorld;
import org.ivandgetic.kube.Layer;
import org.ivandgetic.renderer.KubeRenderer;

public class MyGLSurfaceView extends GLSurfaceView {
    // names for our 9 layers (based on notation from http://www.cubefreak.net/notation.html)
    static final int kUp = 0;
    static final int kDown = 1;
    static final int kLeft = 2;
    static final int kRight = 3;
    static final int kFront = 4;
    static final int kBack = 5;
    static final int kMiddle = 6;
    static final int kEquator = 7;
    static final int kSide = 8;
    // a Layer for each possible move
    public static Layer[] mLayers = new Layer[9];
    // current permutation of starting position
    public static int[] mPermutation;
    static Cube[] mCubes = new Cube[27];
    private KubeRenderer mRenderer;//具体实现的渲染器
    private float mPreviousX, mPreviousY;//记录上次触屏位置的坐标
    private float tempX, tempY;
    private Context context;
    private MediaPlayer mediaPlayer;

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        mRenderer = new KubeRenderer(context, makeGLWorld());
        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);// 透视上一个Activity
        setRenderer(mRenderer);// 设置渲染器
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);// 设置渲染模式为主动渲染
        mediaPlayer = MediaPlayer.create(context, R.raw.button);
    }

    public static void updateLayers() {
        Layer layer;
        GLShape[] shapes;
        int i, j, k;

        // up layer
        layer = mLayers[kUp];
        shapes = layer.mShapes;
        for (i = 0; i < 9; i++)
            shapes[i] = mCubes[mPermutation[i]];

        // down layer
        layer = mLayers[kDown];
        shapes = layer.mShapes;
        for (i = 18, k = 0; i < 27; i++)
            shapes[k++] = mCubes[mPermutation[i]];

        // left layer
        layer = mLayers[kLeft];
        shapes = layer.mShapes;
        for (i = 0, k = 0; i < 27; i += 9)
            for (j = 0; j < 9; j += 3)
                shapes[k++] = mCubes[mPermutation[i + j]];

        // right layer
        layer = mLayers[kRight];
        shapes = layer.mShapes;
        for (i = 2, k = 0; i < 27; i += 9)
            for (j = 0; j < 9; j += 3)
                shapes[k++] = mCubes[mPermutation[i + j]];

        // front layer
        layer = mLayers[kFront];
        shapes = layer.mShapes;
        for (i = 6, k = 0; i < 27; i += 9)
            for (j = 0; j < 3; j++)
                shapes[k++] = mCubes[mPermutation[i + j]];

        // back layer
        layer = mLayers[kBack];
        shapes = layer.mShapes;
        for (i = 0, k = 0; i < 27; i += 9)
            for (j = 0; j < 3; j++)
                shapes[k++] = mCubes[mPermutation[i + j]];

        // middle layer
        layer = mLayers[kMiddle];
        shapes = layer.mShapes;
        for (i = 1, k = 0; i < 27; i += 9)
            for (j = 0; j < 9; j += 3)
                shapes[k++] = mCubes[mPermutation[i + j]];

        // equator layer
        layer = mLayers[kEquator];
        shapes = layer.mShapes;
        for (i = 9, k = 0; i < 18; i++)
            shapes[k++] = mCubes[mPermutation[i]];

        // side layer
        layer = mLayers[kSide];
        shapes = layer.mShapes;
        for (i = 3, k = 0; i < 27; i += 9)
            for (j = 0; j < 3; j++)
                shapes[k++] = mCubes[mPermutation[i + j]];
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        AppConfig.setTouchPosition(x, y);
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = y - mPreviousY;
                float dy = x - mPreviousX;
                float d = (float) (Math.sqrt(dx * dx + dy * dy));// 手势距离
                // 旋转轴单位向量的x,y值（z=0）
                if (AppConfig.startRotate) {
                    mRenderer.mfAngleX = dx;
                    mRenderer.mfAngleY = dy;
                    mRenderer.gesDistance = d;// 手势距离
                }
                AppConfig.gbNeedPick = false;
                break;
            case MotionEvent.ACTION_DOWN:
                tempX = e.getX();
                tempY = e.getY();
                System.out.println("X:" + tempX);
                System.out.println("Y:" + tempY);
                AppConfig.gbNeedPick = true;
                break;
            case MotionEvent.ACTION_UP:
                AppConfig.direct = "";
                if (Math.abs(e.getX() - tempX) > Math.abs(e.getY() - tempY)) {//左右
                    if (e.getX() - tempX > 0) {//右
                        AppConfig.direct = "右";
                    } else if (e.getX() - tempX < 0) {//左
                        AppConfig.direct = "左";
                    }
                } else {//上下
                    if (e.getY() - tempY > 0) {//下
                        AppConfig.direct = "下";
                    } else if (e.getY() - tempY < 0) {//上
                        AppConfig.direct = "上";
                    }
                }
                gogogo();
                AppConfig.gbNeedPick = false;
                AppConfig.startRotate = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                AppConfig.gbNeedPick = false;
                break;
        }
        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    public void gogogo() {
        int mian = 0;
        if (AppConfig.direct.length() > 0 && AppConfig.canRotate) {
            boolean direction = false;
            if (AppConfig.direct.equals("上")) {
                if (AppConfig.currentNumber == 4) {
                    direction = true;
                } else if (AppConfig.currentNumber == 5) {
                    direction = false;
                }
                if (AppConfig.currentNumber == 0) {
                    direction = false;
                } else if (AppConfig.currentNumber == 1) {
                    direction = true;
                }
            } else if (AppConfig.direct.equals("下")) {
                if (AppConfig.currentNumber == 4) {
                    direction = false;
                } else if (AppConfig.currentNumber == 5) {
                    direction = true;
                }
                if (AppConfig.currentNumber == 0) {
                    direction = true;
                } else if (AppConfig.currentNumber == 1) {
                    direction = false;
                }
            }
            if (AppConfig.direct.equals("左")) {
                if (AppConfig.currentNumber == 0) {
                    direction = true;
                } else if (AppConfig.currentNumber == 1) {
                    direction = true;
                }
            } else if (AppConfig.direct.equals("右")) {
                if (AppConfig.currentNumber == 0) {
                    direction = false;
                } else if (AppConfig.currentNumber == 1) {
                    direction = false;
                }
            }

            if (AppConfig.direct.equals("左") || AppConfig.direct.equals("右")) {
                if (tempY > 414 && tempY < 585)
                    mian = 7;
                else if (tempY > 585)
                    mian = 1;
                else if (tempY < 414)
                    mian = 0;
            } else if (AppConfig.direct.equals("上") || AppConfig.direct.equals("下")) {
                if (tempX > 461 && AppConfig.currentNumber == 4 || tempX > 461 && AppConfig.currentNumber == 2 || tempX > 461 && AppConfig.currentNumber == 5 || tempX > 461 && AppConfig.currentNumber == 3)
                    mian = 5;
                else if (tempX < 275 && AppConfig.currentNumber == 4 || tempX < 275 && AppConfig.currentNumber == 2 || tempX < 275 && AppConfig.currentNumber == 5 || tempX < 275 && AppConfig.currentNumber == 3)
                    mian = 4;
                else if (tempX > 275 && tempX < 461 && AppConfig.currentNumber == 4 || tempX > 275 && tempX < 461 && AppConfig.currentNumber == 2 || tempX > 275 && tempX < 461 && AppConfig.currentNumber == 5 || tempX > 275 && tempX < 461 && AppConfig.currentNumber == 3)
                    mian = 8;
                else if (tempX > 275 && tempX < 461 && AppConfig.currentNumber == 0 || tempX > 275 && tempX < 461 && AppConfig.currentNumber == 1 || tempX > 275 && tempX < 461 && AppConfig.currentNumber == 2 || tempX > 275 && tempX < 461 && AppConfig.currentNumber == 3)
                    mian = 6;
                else if (tempX > 461 && AppConfig.currentNumber == 0 || tempX > 461 && AppConfig.currentNumber == 1 || tempX > 461 && AppConfig.currentNumber == 2 || tempX > 461 && AppConfig.currentNumber == 3)
                    mian = 3;
                else if (tempX < 275 && AppConfig.currentNumber == 0 || tempX < 275 && AppConfig.currentNumber == 1 || tempX < 275 && AppConfig.currentNumber == 2 || tempX < 275 && AppConfig.currentNumber == 3)
                    mian = 2;
            }
            System.out.println(mian + "," + direction);
            playSound();
            AppConfig.mian = mian;
            AppConfig.direction = direction;
            AppConfig.isRotare = true;
        }
    }

    private GLWorld makeGLWorld() {
        GLWorld world = new GLWorld();

        int one = 0x10000;
        int half = 0x08000;
        GLColor red = new GLColor(one, 0, 0);
        GLColor green = new GLColor(0, one, 0);
        GLColor blue = new GLColor(0, 0, one);
        GLColor yellow = new GLColor(one, one, 0);
        GLColor orange = new GLColor(one, half, 0);
        GLColor white = new GLColor(one, one, one);
        GLColor black = new GLColor(0, 0, 0);

        // coordinates for our cubes
        float c0 = -1.0f;
        float c1 = -0.38f;
        float c2 = -0.32f;
        float c3 = 0.32f;
        float c4 = 0.38f;
        float c5 = 1.0f;

        // top back, left to right
        mCubes[0] = new org.ivandgetic.kube.Cube(world, c0, c4, c0, c1, c5, c1);
        mCubes[1] = new org.ivandgetic.kube.Cube(world, c2, c4, c0, c3, c5, c1);
        mCubes[2] = new org.ivandgetic.kube.Cube(world, c4, c4, c0, c5, c5, c1);
        // top middle, left to right
        mCubes[3] = new org.ivandgetic.kube.Cube(world, c0, c4, c2, c1, c5, c3);
        mCubes[4] = new org.ivandgetic.kube.Cube(world, c2, c4, c2, c3, c5, c3);
        mCubes[5] = new org.ivandgetic.kube.Cube(world, c4, c4, c2, c5, c5, c3);
        // top front, left to right
        mCubes[6] = new org.ivandgetic.kube.Cube(world, c0, c4, c4, c1, c5, c5);
        mCubes[7] = new org.ivandgetic.kube.Cube(world, c2, c4, c4, c3, c5, c5);
        mCubes[8] = new org.ivandgetic.kube.Cube(world, c4, c4, c4, c5, c5, c5);
        // middle back, left to right
        mCubes[9] = new org.ivandgetic.kube.Cube(world, c0, c2, c0, c1, c3, c1);
        mCubes[10] = new org.ivandgetic.kube.Cube(world, c2, c2, c0, c3, c3, c1);
        mCubes[11] = new org.ivandgetic.kube.Cube(world, c4, c2, c0, c5, c3, c1);
        // middle middle, left to right
        mCubes[12] = new org.ivandgetic.kube.Cube(world, c0, c2, c2, c1, c3, c3);
        mCubes[13] = null;
        mCubes[14] = new org.ivandgetic.kube.Cube(world, c4, c2, c2, c5, c3, c3);
        // middle front, left to right
        mCubes[15] = new org.ivandgetic.kube.Cube(world, c0, c2, c4, c1, c3, c5);
        mCubes[16] = new org.ivandgetic.kube.Cube(world, c2, c2, c4, c3, c3, c5);
        mCubes[17] = new org.ivandgetic.kube.Cube(world, c4, c2, c4, c5, c3, c5);
        // bottom back, left to right
        mCubes[18] = new org.ivandgetic.kube.Cube(world, c0, c0, c0, c1, c1, c1);
        mCubes[19] = new org.ivandgetic.kube.Cube(world, c2, c0, c0, c3, c1, c1);
        mCubes[20] = new org.ivandgetic.kube.Cube(world, c4, c0, c0, c5, c1, c1);
        // bottom middle, left to right
        mCubes[21] = new org.ivandgetic.kube.Cube(world, c0, c0, c2, c1, c1, c3);
        mCubes[22] = new org.ivandgetic.kube.Cube(world, c2, c0, c2, c3, c1, c3);
        mCubes[23] = new org.ivandgetic.kube.Cube(world, c4, c0, c2, c5, c1, c3);
        // bottom front, left to right
        mCubes[24] = new org.ivandgetic.kube.Cube(world, c0, c0, c4, c1, c1, c5);
        mCubes[25] = new org.ivandgetic.kube.Cube(world, c2, c0, c4, c3, c1, c5);
        mCubes[26] = new org.ivandgetic.kube.Cube(world, c4, c0, c4, c5, c1, c5);

        // paint the sides
        int i, j;
        // set all faces black by default
        for (i = 0; i < 27; i++) {
            org.ivandgetic.kube.Cube cube = mCubes[i];
            if (cube != null) {
                for (j = 0; j < 6; j++)
                    cube.setFaceColor(j, black);
            }
        }

        // paint top
        for (i = 0; i < 9; i++)
            mCubes[i].setFaceColor(org.ivandgetic.kube.Cube.kTop, orange);
        // paint bottom
        for (i = 18; i < 27; i++)
            mCubes[i].setFaceColor(org.ivandgetic.kube.Cube.kBottom, red);
        // paint left
        for (i = 0; i < 27; i += 3)
            mCubes[i].setFaceColor(org.ivandgetic.kube.Cube.kLeft, yellow);
        // paint right
        for (i = 2; i < 27; i += 3)
            mCubes[i].setFaceColor(org.ivandgetic.kube.Cube.kRight, white);
        // paint back
        for (i = 0; i < 27; i += 9)
            for (j = 0; j < 3; j++)
                mCubes[i + j].setFaceColor(org.ivandgetic.kube.Cube.kBack, blue);
        // paint front
        for (i = 6; i < 27; i += 9)
            for (j = 0; j < 3; j++)
                mCubes[i + j].setFaceColor(org.ivandgetic.kube.Cube.kFront, green);

        for (i = 0; i < 27; i++)
            if (mCubes[i] != null)
                world.addShape(mCubes[i]);

        // initialize our permutation to solved position
        mPermutation = new int[27];
        for (i = 0; i < mPermutation.length; i++)
            mPermutation[i] = i;

        createLayers();
        updateLayers();

        world.generate();

        return world;
    }

    private void createLayers() {
        mLayers[kUp] = new Layer(Layer.kAxisY);
        mLayers[kDown] = new Layer(Layer.kAxisY);
        mLayers[kLeft] = new Layer(Layer.kAxisX);
        mLayers[kRight] = new Layer(Layer.kAxisX);
        mLayers[kFront] = new Layer(Layer.kAxisZ);
        mLayers[kBack] = new Layer(Layer.kAxisZ);
        mLayers[kMiddle] = new Layer(Layer.kAxisX);
        mLayers[kEquator] = new Layer(Layer.kAxisY);
        mLayers[kSide] = new Layer(Layer.kAxisZ);
    }

    public void playSound() {
        if (AppConfig.playSound) {
            mediaPlayer.start();
        }
    }
}
