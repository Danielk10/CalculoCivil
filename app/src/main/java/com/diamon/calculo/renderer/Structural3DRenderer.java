package com.diamon.calculo.renderer;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * OpenGL ES 3.0 renderer for 3D structural visualization.
 * Renders nodes, frame elements, deformed shapes, and mode shapes.
 */
public class Structural3DRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "Structural3DRenderer";

    // Shader sources (GLSL ES 3.0)
    private static final String VERTEX_SHADER =
            "#version 300 es\n" +
            "uniform mat4 uMVPMatrix;\n" +
            "uniform float uPointSize;\n" +
            "in vec3 aPosition;\n" +
            "in vec4 aColor;\n" +
            "out vec4 vColor;\n" +
            "void main() {\n" +
            "    gl_Position = uMVPMatrix * vec4(aPosition, 1.0);\n" +
            "    gl_PointSize = uPointSize;\n" +
            "    vColor = aColor;\n" +
            "}\n";

    private static final String FRAGMENT_SHADER =
            "#version 300 es\n" +
            "precision mediump float;\n" +
            "in vec4 vColor;\n" +
            "out vec4 fragColor;\n" +
            "void main() {\n" +
            "    fragColor = vColor;\n" +
            "}\n";

    // Matrices
    private final float[] mvpMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] tempMatrix = new float[16];

    // Camera
    private float rotationX = 30f;
    private float rotationY = -45f;
    private float translationX = 0f;
    private float translationY = 0f;
    private float zoom = 15f;
    private float deformationScale = 1.0f;
    private boolean usePerspective = true;

    // Shader program
    private int programId;
    private int mvpMatrixHandle;
    private int positionHandle;
    private int colorHandle;
    private int pointSizeHandle;

    // VBOs
    private int[] nodeVBO = new int[2]; // position, color
    private int[] elemVBO = new int[2]; // position, color
    private int[] gridVBO = new int[2]; // position, color

    // Data counts
    private int nodeCount = 0;
    private int elemVertexCount = 0;
    private int gridVertexCount = 0;
    private boolean hasModel = false;

    // Deformed shape data
    private float[] deformedPositions = null;
    private float[] deformedColors = null;
    private int deformedVertexCount = 0;
    private int[] deformedVBO = new int[2];
    private boolean showDeformed = false;

    // Load vectors & diagrams data
    private int[] loadVBO = new int[2];
    private int loadVertexCount = 0;
    private int[] diagramVBO = new int[2];
    private int diagramVertexCount = 0;
    private boolean showDiagrams = true;

    // Screen dimensions
    private int screenWidth = 1;
    private int screenHeight = 1;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(0.08f, 0.08f, 0.12f, 1.0f); // Dark blue-gray background
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glLineWidth(2.0f);

        programId = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (programId == 0) {
            Log.e(TAG, "Failed to create shader program");
            return;
        }

        mvpMatrixHandle = GLES30.glGetUniformLocation(programId, "uMVPMatrix");
        positionHandle = GLES30.glGetAttribLocation(programId, "aPosition");
        colorHandle = GLES30.glGetAttribLocation(programId, "aColor");
        pointSizeHandle = GLES30.glGetUniformLocation(programId, "uPointSize");

        // Generate VBOs
        GLES30.glGenBuffers(2, nodeVBO, 0);
        GLES30.glGenBuffers(2, elemVBO, 0);
        GLES30.glGenBuffers(2, gridVBO, 0);
        GLES30.glGenBuffers(2, deformedVBO, 0);
        GLES30.glGenBuffers(2, loadVBO, 0);
        GLES30.glGenBuffers(2, diagramVBO, 0);

        createGrid();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        screenWidth = width;
        screenHeight = height;
        updateProjectionMatrix();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        if (programId == 0) return;
        GLES30.glUseProgram(programId);

        // View matrix - orbit camera
        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.translateM(viewMatrix, 0, translationX, translationY, -zoom);
        Matrix.rotateM(viewMatrix, 0, rotationX, 1f, 0f, 0f);
        Matrix.rotateM(viewMatrix, 0, rotationY, 0f, 1f, 0f);

        // Model matrix
        Matrix.setIdentityM(modelMatrix, 0);

        // MVP = Projection * View * Model
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0);

        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw grid
        if (gridVertexCount > 0) {
            GLES30.glUniform1f(pointSizeHandle, 1.0f);
            drawVBO(gridVBO, gridVertexCount, GLES30.GL_LINES);
        }

        if (hasModel) {
            // Draw elements
            if (elemVertexCount > 0 && !showDeformed) {
                drawVBO(elemVBO, elemVertexCount, GLES30.GL_LINES);
            }

            // Draw deformed shape
            if (showDeformed && deformedVertexCount > 0) {
                drawVBO(deformedVBO, deformedVertexCount, GLES30.GL_LINES);
            }

            // Draw loads (force arrows)
            if (loadVertexCount > 0) {
                GLES30.glLineWidth(3.0f);
                drawVBO(loadVBO, loadVertexCount, GLES30.GL_LINES);
                GLES30.glLineWidth(2.0f);
            }

            // Draw diagrams (moment/shear)
            if (showDiagrams && diagramVertexCount > 0) {
                GLES30.glLineWidth(2.5f);
                drawVBO(diagramVBO, diagramVertexCount, GLES30.GL_LINES);
                GLES30.glLineWidth(2.0f);
            }

            // Draw nodes
            if (nodeCount > 0) {
                GLES30.glUniform1f(pointSizeHandle, 10.0f);
                drawVBO(nodeVBO, nodeCount, GLES30.GL_POINTS);
            }
        }
    }

    // ==================== Public API ====================

    public void setRotation(float angleX, float angleY) {
        this.rotationX = angleX;
        this.rotationY = angleY;
    }

    public void addRotation(float dx, float dy) {
        this.rotationY += dx;
        this.rotationX += dy;
        // Clamp elevation
        if (rotationX > 89f) rotationX = 89f;
        if (rotationX < -89f) rotationX = -89f;
    }

    public void setTranslation(float dx, float dy) {
        this.translationX += dx;
        this.translationY += dy;
    }

    public void setZoom(float scale) {
        this.zoom *= scale;
        if (this.zoom < 1f) this.zoom = 1f;
        if (this.zoom > 100f) this.zoom = 100f;
    }

    public void setDeformationScale(float scale) {
        this.deformationScale = scale;
    }

    public void setPerspective(boolean perspective) {
        this.usePerspective = perspective;
        updateProjectionMatrix();
    }

    public void setShowDeformed(boolean show) {
        this.showDeformed = show;
    }

    public void setNodes(float[] positions, float[] colors) {
        if (positions == null || positions.length == 0) return;
        nodeCount = positions.length / 3;
        uploadVBO(nodeVBO, positions, colors);
        hasModel = true;
    }

    public void setElements(float[] positions, float[] colors) {
        if (positions == null || positions.length == 0) return;
        elemVertexCount = positions.length / 3;
        uploadVBO(elemVBO, positions, colors);
        hasModel = true;
    }

    public void setDeformedShape(float[] positions, float[] colors) {
        if (positions == null || positions.length == 0) return;
        deformedVertexCount = positions.length / 3;
        deformedPositions = positions;
        deformedColors = colors;
        uploadVBO(deformedVBO, positions, colors);
    }

    public void setLoads(float[] positions, float[] colors) {
        if (positions == null || positions.length == 0) {
            loadVertexCount = 0;
            return;
        }
        loadVertexCount = positions.length / 3;
        uploadVBO(loadVBO, positions, colors);
    }

    public void setDiagrams(float[] positions, float[] colors) {
        if (positions == null || positions.length == 0) {
            diagramVertexCount = 0;
            return;
        }
        diagramVertexCount = positions.length / 3;
        uploadVBO(diagramVBO, positions, colors);
    }

    public void setShowDiagrams(boolean show) {
        this.showDiagrams = show;
    }

    public boolean isShowDiagrams() {
        return showDiagrams;
    }

    public void clearModel() {
        nodeCount = 0;
        elemVertexCount = 0;
        deformedVertexCount = 0;
        loadVertexCount = 0;
        diagramVertexCount = 0;
        hasModel = false;
        showDeformed = false;
    }

    // ==================== Private Helpers ====================

    private void updateProjectionMatrix() {
        float ratio = (float) screenWidth / screenHeight;
        if (usePerspective) {
            Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 0.1f, 200f);
        } else {
            float orthoScale = zoom / 3f;
            Matrix.orthoM(projectionMatrix, 0, -ratio * orthoScale, ratio * orthoScale,
                    -orthoScale, orthoScale, 0.1f, 200f);
        }
    }

    private void createGrid() {
        int gridSize = 10;
        int lineCount = (gridSize * 2 + 1) * 2;
        float[] positions = new float[lineCount * 2 * 3];
        float[] colors = new float[lineCount * 2 * 4];

        int idx = 0;
        int cidx = 0;
        for (int i = -gridSize; i <= gridSize; i++) {
            // Lines along X
            positions[idx++] = i; positions[idx++] = 0; positions[idx++] = -gridSize;
            positions[idx++] = i; positions[idx++] = 0; positions[idx++] = gridSize;
            // Lines along Z
            positions[idx++] = -gridSize; positions[idx++] = 0; positions[idx++] = i;
            positions[idx++] = gridSize; positions[idx++] = 0; positions[idx++] = i;

            float alpha = (i == 0) ? 0.5f : 0.15f;
            for (int j = 0; j < 4; j++) {
                colors[cidx++] = 0.4f; colors[cidx++] = 0.4f; colors[cidx++] = 0.5f; colors[cidx++] = alpha;
            }
        }

        gridVertexCount = idx / 3;
        uploadVBO(gridVBO, positions, colors);
    }

    private void uploadVBO(int[] vbo, float[] positions, float[] colors) {
        FloatBuffer posBuffer = createFloatBuffer(positions);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, positions.length * 4, posBuffer, GLES30.GL_STATIC_DRAW);

        FloatBuffer colorBuffer = createFloatBuffer(colors);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[1]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, colors.length * 4, colorBuffer, GLES30.GL_STATIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    private void drawVBO(int[] vbo, int vertexCount, int drawMode) {
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0]);
        GLES30.glEnableVertexAttribArray(positionHandle);
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 0, 0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[1]);
        GLES30.glEnableVertexAttribArray(colorHandle);
        GLES30.glVertexAttribPointer(colorHandle, 4, GLES30.GL_FLOAT, false, 0, 0);

        GLES30.glDrawArrays(drawMode, 0, vertexCount);

        GLES30.glDisableVertexAttribArray(positionHandle);
        GLES30.glDisableVertexAttribArray(colorHandle);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }

    private FloatBuffer createFloatBuffer(float[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(data);
        fb.position(0);
        return fb;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource);
        if (vertexShader == 0 || fragmentShader == 0) return 0;

        int program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        GLES30.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            Log.e(TAG, "Program link error: " + GLES30.glGetProgramInfoLog(program));
            GLES30.glDeleteProgram(program);
            return 0;
        }
        return program;
    }

    private int loadShader(int type, String source) {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, source);
        GLES30.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Shader compile error: " + GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }
}
