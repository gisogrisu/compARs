/****************************** CompARs ******************************
* TODO:	
* 	Add black edges (GL_LINE) to arrow
*	Description, Author, etc
*********************************************************************/
package de.noah.naime.compars;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setLookAtM;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import de.noah.naime.compars.util.Arrow;
import de.noah.naime.compars.util.MatrixHelper;
import de.noah.naime.compars.util.ShaderHelper;
import de.noah.naime.compars.util.TextResourceReader;

public class ArrowRenderer implements Renderer {
	
	private static final String A_COLOR = "a_Color";
	private static final String A_POSITION = "a_Position"; 
	private static final String U_MATRIX = "u_Matrix";
	private final Context context;
	// Id of linked OpenGL ES program
	private int program;
	// Set colour of what we are drawing
	private int aColorLocation;
	// Set location of attribute
	private int aPositionLocation;
	// Matrix for othogonal projection
	private final float[] projectionMatrix = new float[16];
	private int uMatrixLocation;
	// Model matrix
	private final float[] modelMatrix = new float[16];
	// View Matrix
	private final float[] viewMatrix = new float[16];
	private final float[] rotatedViewMatrix = new float[16];
	// Hold results of matrix multiplications
	private final float[] viewProjectionMatrix = new float[16];
	private final float[] modelViewProjectionMatrix = new float[16];
	// Rotate view
	private float[] rotationMatrix = new float[16];
	
	private Arrow arrow;
	
	// Constructor
	public ArrowRenderer(Context context) {
		this.context = context;
	}

	// Create surface
	@Override
	public void onSurfaceCreated(GL10 glUnused, 
			javax.microedition.khronos.egl.EGLConfig config) {
		
		// Set background to black
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// Create arrow vertices
		arrow = new Arrow();
		
		// Read in vertex shader from glsl code
		String vertexShaderSource = TextResourceReader
			.readTextFileFromResource(context, R.raw.vertex_shader);
			
		// Read in fragment shader from glsl code
		String fragmentShaderSource = TextResourceReader
			.readTextFileFromResource(context, R.raw.fragment_shader);

		// Compile the shader code
		int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
		int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);

		// Vertex and fragment shaders always go together
		program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

		// Now use the program
		glUseProgram(program);               

		// Get positions of Matrix, attribute and colour
		uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
		aPositionLocation = glGetAttribLocation(program, A_POSITION);
		aColorLocation = glGetAttribLocation(program, A_COLOR);
	}
	
	// Size has changed (eg. switch from landscape to portait)
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
	
		// Tell OpenGL the size of the surface
		glViewport(0, 0, width, height);
		
		// Move arrow into the distance
		MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width / (float) height, 1f, 10f);
		
		// Create viewMatrix
		// Eye will be at 0, 0, 3f (eye will be straight on the x-z plane and 3 units in front of screen)
		// Center will be at 0f, 0f, 0f
		// upX, upY, upZ: Head will be pointing straight up and scene won't be rotated
		setLookAtM(viewMatrix, 0, 0, 0, 3f, 0f, 0f, 0f, 0f, 1f, 0f);

		// Set rotation to 0
		setIdentityM(rotationMatrix, 0);
		multiplyMM(rotatedViewMatrix, 0, viewMatrix, 0, rotationMatrix, 0	);
	}

	// Draw a frame
	@Override
	public void onDrawFrame(GL10 glUnused) {
	
		// Clear the screen
		glClear(GL_COLOR_BUFFER_BIT);
		
		// Multiply projection and view matrices into viewProjectionMatrix
		multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, rotatedViewMatrix, 0);
		
		// Position arrow in scene
		setIdentityM(modelMatrix, 0);
		multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
		
		// SetUniform
		glUniformMatrix4fv(uMatrixLocation, 1, false, modelViewProjectionMatrix, 0);
		
		// Draw the arrow
		arrow.bindData(aPositionLocation, aColorLocation);
		arrow.draw();
	}
	
	public void rotateViewMatrix(float[] rm){
		
		rotationMatrix = rm;
		
		multiplyMM(rotatedViewMatrix, 0, viewMatrix, 0, rotationMatrix, 0);
	}
}