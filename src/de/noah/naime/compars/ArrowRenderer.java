/****************************** CompARs ******************************
* TODO:	OpenGlES Chapter 6
* 	Remove menu bar
*	Description, Author, etc
* 	Change to british english
*********************************************************************/
package de.noah.naime.compars;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.orthoM;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import de.noah.naime.compars.util.ShaderHelper;
import de.noah.naime.compars.util.TextResourceReader;

public class ArrowRenderer implements Renderer {
	
	private static final String A_COLOR = "a_Color";
	private static final String A_POSITION = "a_Position";  
	private static final int POSITION_COMPONENT_COUNT = 2;
	private static final int COLOR_COMPONENT_COUNT = 3;
	private static final int BYTES_PER_FLOAT = 4;
	// Skip over the colour bytes for the current vertex
	private static final int STRIDE = 
		(POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
	private static final String U_MATRIX = "u_Matrix";
	// Store data in in native memory
	private final FloatBuffer vertexData;
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
	
	// Constructor
	public ArrowRenderer(Context context) {
		this.context = context;
		
		float[] arrowVertices = {
			// Order of coordinates: X, Y, Z, W, R, G, B
			
			// Head
//			0,	0.4f,	-0.1f,	1f,	0.5f,	1f,	0.5f,	// Top
//			-0.4f,	0.1f,	-0.1f,	1f,	0.5f,	1f,	0.5f,	// Left
//			0.4f,	0.1f,	-0.1f,	1f,	0.5f,	1f,	0.5f,	// Right
			
			// Tail - Triangle 1
//			-0.2f,	0.1f,	-0.1f,	1f,	0.4f,	1f,	0.4f,	// Top left
//			0.2f,	0.1f,	-0.1f,	1f,	0.4f,	1f,	0.4f,	// Top right
//			-0.2f,	-0.3f,	-0.1f,	1f,	0.4f,	1f,	0.4f,	// Bottom left
			
			// Tail - Triangle 2
//			-0.2f,	-0.3f,	-0.1f,	1f,	0.4f,	1f,	0.4f,	// Bottom left
//			0.2f,	-0.3f,	-0.1f,	1f,	0.4f,	1f,	0.4f,	// Bottom right
//			0.2f,	0.1f,	-0.1f,	1f,	0.4f,	1f,	0.4f,	// Top right

			// Order of coordinates: X, Y, R, G, B
			// Head
			0,	0.5f,	1f,	0.2f,	0.2f,	// Top
			-0.5f,	0.1f,	1f,	0.2f,	0.2f,	// Left
			0.5f,	0.1f,	1f,	0.2f,	0.2f,	// Right
			
			// Tail - Triangle Fan
			0,	-0.1f,	0.2f,	1f,	0.2f,	// Middle
			-0.25f,	-0.5f,	0.2f,	1f,	0.2f,	// Bottom left
			0.25f,	-0.5f,	0.2f,	1f,	0.2f,	// Bottom right
			0.25f,	0.1f,	1f,	0.2f,	0.2f,	// Top right
			-0.25f,	0.1f,	1f,	0.2f,	0.2f,	// Top left
			-0.25f,	-0.5f,	0.2f,	1f,	0.2f,	// Bottom left
		};       

		
		vertexData = ByteBuffer
			// allocate a block of native memory
			.allocateDirect(arrowVertices.length * BYTES_PER_FLOAT)
			// use same byte order as platform
			.order(ByteOrder.nativeOrder())
			// we want to work with floats
			.asFloatBuffer();

		vertexData.put(arrowVertices);
	}

	// Create surface
	@Override
	public void onSurfaceCreated(GL10 glUnused, 
			javax.microedition.khronos.egl.EGLConfig config) {
		
		// Set background to black
		 glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

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

		// Ensure reading the data from the beginning
		vertexData.position(0);
		// Tell OpenGl it can find the data for a_Position in the buffer vertexData
		glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, 
			false, STRIDE, vertexData);

		// Enable the attribute
		glEnableVertexAttribArray(aPositionLocation);
		  
		// Start reading at first colour attribute
		vertexData.position(POSITION_COMPONENT_COUNT);
		// Tell OpenGl it can find the data for a_Position in the buffer vertexData
		glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT, 
			false, STRIDE, vertexData);

		// Enable the attribute
		glEnableVertexAttribArray(aColorLocation);
	}
	
	// Size has changed (eg. switch from landscape to portait)
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
	
		// Tell OpenGL the size of the surface
		glViewport(0, 0, width, height);
        
		final float aspectRatio = width > height ? 
			(float) width / (float) height : 
			(float) height / (float) width;

		if (width > height) {
			// Landscape
			orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
		} else {
			// Portrait or square
			orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
		}   
	}

	// Draw a frame
	@Override
	public void onDrawFrame(GL10 glUnused) {
	
		// Clear the screen
		glClear(GL_COLOR_BUFFER_BIT);
		
		// Add orthographic projection
		glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);
		
		// Draw Arrow head
		// Draw triangles, start at beginning of vertex array, read in three vertices
		glDrawArrays(GL_TRIANGLES, 0, 3);
		
		// Draw Arrow tail
		// Draw triangle fan, start after first object, read in six vertices
		glDrawArrays(GL_TRIANGLE_FAN, 3, 6);
		
	}
}