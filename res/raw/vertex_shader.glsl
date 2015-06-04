/*************************** Vertex Shader ***************************
* generates the final position of each vertex and is run once per
* vertex. Once the final positions are known, OpenGL will take the
* visible set of vertices and assemble them into points, lines, and
* triangles.
*********************************************************************/

// Matrix for orthographic projection
uniform mat4 u_Matrix;

// Current vertex position in vector (x, y, z, w)
attribute vec4 a_Position;  
attribute vec4 a_Color;

// Blend the given values
varying vec4 v_Color;

void main()                    
{                            
	v_Color = a_Color;
	
	// Do orthographic projection and copy to output
	gl_Position = u_Matrix * a_Position;
	
	// Define size of GL_POINTS
	gl_PointSize = 10.0;          
}          