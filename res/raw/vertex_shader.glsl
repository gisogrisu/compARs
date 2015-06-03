/*************************** Vertex Shader ***************************
* generates the final position of each vertex and is run once per
* vertex. Once the final positions are known, OpenGL will take the
* visible set of vertices and assemble them into points, lines, and
* triangles.
*********************************************************************/

// uniform mat4 u_Matrix;

// Current vertex position in vector (x, y, z, w)
attribute vec4 a_Position;  
// attribute vec4 a_Color;

// varying vec4 v_Color;

void main()                    
{                            
//	v_Color = a_Color;
	
	// Copy position to output
//	gl_Position = u_Matrix * a_Position;
	gl_Position = a_Position;
	
	gl_PointSize = 10.0;          
}          