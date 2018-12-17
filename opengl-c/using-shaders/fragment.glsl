#version 440 core
uniform dvec2 delta;
uniform dvec2 mins;
uniform int iters;

in vec3 pos3d;

out vec3 fragColor;

void main()
{
	dvec2 z = dvec2(0, 0);
	dvec2 c = dvec2(gl_FragCoord.x * delta.x + mins.x, gl_FragCoord.y * delta.y + mins.y);
	for(int i = 0; i < iters; ++i){
		z = dvec2(z.x * z.x - z.y * z.y, 2 * z.x * z.y) + c;
		if(z.x * z.x + z.y * z.y >= 4){
			float pct = float(i)/float(iters);
			fragColor = vec3(pct, pct*pct, pow(2*pct - 1, 2));
			return;
		}
	}
	fragColor = vec3(0, 0, 0);
}