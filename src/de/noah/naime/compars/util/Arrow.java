package de.noah.naime.compars.util;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Arrow {
	
	private static final int POSITION_COMPONENT_COUNT = 3;
	private static final int COLOR_COMPONENT_COUNT = 3;
	private static final int BYTES_PER_FLOAT = 4;
	// Skip over the colour bytes for the current vertex
	private static final int STRIDE = 
		(POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
	
	// Store data in in native memory
	private final FloatBuffer vertexData;
	
	// Constructor
	public Arrow(){
		
		// Create vertices
		float[] arrowVertices = {
		// Order of coordinates: X, Y, Z, R, G, B
		
		// Rim - Head Bottom left (Triangle Strip)
		-0.25f,	0.1f,	0,	1f,	0,	0,	// Top right (Z)
		-0.25f,	0.1f,	-0.2f,	1f,	0,	0,	// Bottom right (Z)
		-0.5f,	0.1f,	0,	1f,	0,	0,	// Top left (Z)
		-0.5f,	0.1f,	-0.2f,	1f,	0,	0,	// Bottom left (Z)
		
		// Rim - Head Bottom right
		0.25f,	0.1f,	0,	1f,	0,	0,	// Top right (Z)
		0.25f,	0.1f,	-0.2f,	1f,	0,	0,	// Bottom right (Z)
		0.5f,	0.1f,	0,	1f,	0,	0,	// Top left (Z)
		0.5f,	0.1f,	-0.2f,	1f,	0,	0,	// Bottom left (Z)
			
		// Rim - Head Top left
		-0.5f,	0.1f,	0,	1f,	0,	0,	// Top left (Z)
		-0.5f,	0.1f,	-0.2f,	1f,	0,	0,	// Bottom left (Z)
		0,	0.5f,	0,	1f,	0,	0,	// Top right (Z)
		0,	0.5f,	-0.2f,	1f,	0,	0,	// Bottom right (Z)
		
		// Rim - Head Top right
		0.5f,	0.1f,	0,	1f,	0,	0,	// Top left (Z)
		0.5f,	0.1f,	-0.2f,	1f,	0,	0,	// Bottom left (Z)
		0,	0.5f,	0,	1f,	0,	0,	// Top right (Z)
		0,	0.5f,	-0.2f,	1f,	0,	0,	// Bottom right (Z)
			
		// Rim - Tail left side (Triangle Strip)
		-0.25f,	-0.5f,	0,	0,	1f,	0,	// Top front (Z)
		-0.25f,	-0.5f,	-0.2f,	0,	1f,	0,	// Bottom front (Z)
		-0.25f,	-0.1f,	0,	0,	1f,	0,	// Top middle (Z)
		-0.25f,	-0.1f,	-0.2f,	0,	1f,	0,	// Bottom middle (Z)
		-0.25f,	0.1f,	0,	1f,	0,	0,	// Top back (Z)
		-0.25f,	0.1f,	-0.2f,	1f,	0,	0,	// Bottom back (Z)
			
		// Rim - Tail right side (Triangle Strip)
		0.25f,	-0.5f,	0,	0,	1f,	0,	// Top front (Z)
		0.25f,	-0.5f,	-0.2f,	0,	1f,	0,	// Bottom front (Z)
		0.25f,	-0.1f,	0,	0,	1f,	0,	// Top middle (Z)
		0.25f,	-0.1f,	-0.2f,	0,	1f,	0,	// Bottom middle (Z)
		0.25f,	0.1f,	0,	1f,	0,	0,	// Top back (Z)
		0.25f,	0.1f,	-0.2f,	1f,	0,	0,	// Bottom back (Z)
		
		// Rim - Tail end (Triangle Strip)
		-0.25f,	-0.5f,	0,	0,	1f,	0,	// Top left (Z)
		-0.25f,	-0.5f,	-0.2f,	0,	1f,	0,	// Bottom left (Z)
		0.25f,	-0.5f,	0,	0,	1f,	0,	// Top right (Z)
		0.25f,	-0.5f,	-0.2f,	0,	1f,	0,	// Bottom right (Z)
			
		// Head
		0,	0.5f,	0,	1f,	0.5f,	0.5f,	// Top
		-0.5f,	0.1f,	0,	1f,	0.5f,	0.5f,	// Left
		0.5f,	0.1f,	0,	1f,	0.5f,	0.5f,	// Right
		
		// Tail (Triangle Fan)
		0,	-0.1f,	0,	0.5f,	1f,	0.5f,	// Middle
		-0.25f,	-0.5f,	0,	0.5f,	1f,	0.5f,	// Bottom left
		0.25f,	-0.5f,	0,	0.5f,	1f,	0.5f,	// Bottom right
		0.25f,	-0.1f,	0,	0.5f,	1f,	0.5f,	// Middle right
		0.25f,	0.1f,	0,	1f,	0.5f,	0.5f,	// Top right
		-0.25f,	0.1f,	0,	1f,	0.5f,	0.5f,	// Top left
		-0.25f,	-0.1f,	0,	0.5f,	1f,	0.5f,	// Middle left
		-0.25f,	-0.5f,	0,	0.5f,	1f,	0.5f,	// Bottom left
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

	public void bindData(int aPositionLocation, int aColorLocation){

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
	
	public void draw(){
		
		// Draw rim first
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);	// Head Bottom left
		glDrawArrays(GL_TRIANGLE_STRIP, 4, 4);	// Head Bottom right
		glDrawArrays(GL_TRIANGLE_STRIP, 8, 4);	// Head Top left
		glDrawArrays(GL_TRIANGLE_STRIP, 12, 4);	// Head Top left
		glDrawArrays(GL_TRIANGLE_STRIP, 16, 6);	// Tail left side
		glDrawArrays(GL_TRIANGLE_STRIP, 22, 6);	// Tail right side
		glDrawArrays(GL_TRIANGLE_STRIP, 28, 4);	// Tail end
		
		// Draw arrow head
		// Draw triangles, start at beginning of vertex array, read in three vertices
		glDrawArrays(GL_TRIANGLES, 32, 3);
		
		// Draw arrow tail
		// Draw triangle fan, start after first object, read in eight vertices
		glDrawArrays(GL_TRIANGLE_FAN, 35, 8);
	}
}
