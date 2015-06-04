/************************** Fragment Shader **************************
* generates the final color of each fragment of a point, line, or
* triangle and is run once per fragment. A fragment is a small,
* rectangular area of a single color, analogous to a pixel on a
* computer screen.
*********************************************************************/

// Default precision for all floating point datatypes in fragment shader
precision mediump float; 

// Blend the given values
varying vec4 v_Color;

void main()
{
	// Copy colour to output
	gl_FragColor = v_Color;
}