/****************************** CompARs ******************************
* TODO:	Description, Author, etc
* 		OpenGlES Chapter 4
*********************************************************************/
package de.noah.naime.compars;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import de.noah.naime.compars.util.ShaderHelper;
import de.noah.naime.compars.util.TextResourceReader;

public class ArrowRenderer implements Renderer {
	
	private static final String U_COLOR = "u_Color";
	private static final String A_POSITION = "a_Position";  
	private static final int POSITION_COMPONENT_COUNT = 2;
	private static final int BYTES_PER_FLOAT = 4;
	
	// Store data in in native memory
	private final FloatBuffer vertexData;
	private final Context context;
	
	// Id of linked OpenGL ES program
	private int program;
	
	// Set colour of what we are drawing
	private int uColorLocation;
	
	// Set location of attribute
	private int aPositionLocation;
	
	// Constructor
	public ArrowRenderer(Context context) {
		this.context = context;
		
		float[] arrowVertices = {
			// Order of coordinates: X, Y, Z, W, R, G, B
			
			// Head
//			0f,	0.4f,	-0.1f,	1f,	0.5f,	1f,	0.5f,	// Top
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

			// Head
			0f,	0.4f,	// Top
			-0.4f,	0.1f,	// Left
			0.4f,	0.1f,	// Right
			
			// Tail - Triangle 1
			-0.2f,	0.1f,	// Top left
			0.2f,	0.1f,	// Top right
			-0.2f,	-0.3f,	// Bottom left
			
			// Tail - Triangle 2
			-0.2f,	-0.3f,	// Bottom left
			0.2f,	-0.3f,	// Bottom right
			0.2f,	0.1f,	// Top right
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

		// Get location of colour
		uColorLocation = glGetUniformLocation(program, U_COLOR);
		
		// Get position of attribute
		aPositionLocation = glGetAttribLocation(program, A_POSITION);
		  
		// Ensure reading the data from the beginning
		vertexData.position(0);
		
		// Tell OpenGl it can find the data for a_Position in the buffer vertexData
		glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, 
			false, 0, vertexData);

		// Enable the attribute
		glEnableVertexAttribArray(aPositionLocation);
	}
	
	// Size has changed (eg. switch from landscape to portait)
	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
	
		// Tell OpenGL the size of the surface
		glViewport(0, 0, width, height);
	}

	// Draw a frame
	@Override
	public void onDrawFrame(GL10 glUnused) {
	
		// Clear the screen
		glClear(GL_COLOR_BUFFER_BIT);
		
		/*** Draw first object ***/
		// Update the value of u_Color in shader
		glUniform4f(uColorLocation, 0.5f, 1.0f, 0.5f, 1.0f);
		
		// Draw triangles, start at beginning of vertex array, read in nine vertices
		glDrawArrays(GL_TRIANGLES, 0, 9);
		
		/*** Uncomment for drawing second object ***/
		// Update the value of u_Color in shader (set to white)
//		glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
		
		// Draw triangles, start after first object, read in seven vertices
//		glDrawArrays(GL_TRIANGLES, 7, 7);
		
	}
}