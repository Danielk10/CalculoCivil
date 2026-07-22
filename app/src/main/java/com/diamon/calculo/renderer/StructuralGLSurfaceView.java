package com.diamon.calculo.renderer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Custom GLSurfaceView with multi-touch gesture handling for structural visualization.
 * Supports orbit rotation, pan, and pinch zoom.
 */
public class StructuralGLSurfaceView extends GLSurfaceView {

    private Structural3DRenderer renderer;
    private ScaleGestureDetector scaleDetector;

    private float previousX;
    private float previousY;
    private int activePointerId = -1;
    private boolean isScaling = false;

    // For two-finger pan
    private float prevMidX, prevMidY;

    public StructuralGLSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public StructuralGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(3);

        renderer = new Structural3DRenderer();
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                renderer.setZoom(1f / scaleFactor);
                isScaling = true;
                return true;
            }
        });
    }

    public Structural3DRenderer getStructuralRenderer() {
        return renderer;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        int action = event.getActionMasked();
        int pointerCount = event.getPointerCount();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                activePointerId = event.getPointerId(0);
                previousX = event.getX();
                previousY = event.getY();
                isScaling = false;
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                if (pointerCount == 2) {
                    prevMidX = (event.getX(0) + event.getX(1)) / 2f;
                    prevMidY = (event.getY(0) + event.getY(1)) / 2f;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (isScaling) break;

                if (pointerCount == 1) {
                    // Single finger: orbit rotation
                    int pointerIndex = event.findPointerIndex(activePointerId);
                    if (pointerIndex < 0) break;

                    float x = event.getX(pointerIndex);
                    float y = event.getY(pointerIndex);
                    float dx = (x - previousX) * 0.3f;
                    float dy = (y - previousY) * 0.3f;

                    renderer.addRotation(dx, dy);

                    previousX = x;
                    previousY = y;
                } else if (pointerCount == 2) {
                    // Two finger: pan
                    float midX = (event.getX(0) + event.getX(1)) / 2f;
                    float midY = (event.getY(0) + event.getY(1)) / 2f;

                    float dx = (midX - prevMidX) * 0.02f;
                    float dy = -(midY - prevMidY) * 0.02f;

                    renderer.setTranslation(dx, dy);

                    prevMidX = midX;
                    prevMidY = midY;
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                activePointerId = -1;
                isScaling = false;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                int pointerIndex = event.getActionIndex();
                int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    int newIndex = pointerIndex == 0 ? 1 : 0;
                    previousX = event.getX(newIndex);
                    previousY = event.getY(newIndex);
                    activePointerId = event.getPointerId(newIndex);
                }
                break;
            }
        }

        requestRender();
        return true;
    }
}
