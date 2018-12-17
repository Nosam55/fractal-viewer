#version 440 core
in vec2 position;
out vec3 pos3d;

void main()
{
	gl_Position = vec4(position, 0, 1);
	pos3d = gl_Position.rgb;
}