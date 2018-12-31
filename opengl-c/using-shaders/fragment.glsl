#version 440 core
uniform dvec2 delta;
uniform dvec2 mins;
uniform dvec2 mu;
uniform int iters;

in vec3 pos3d;

out vec3 fragColor;

void main()
{
	//swap z and c
	dvec2 z = dvec2(gl_FragCoord.x * delta.x + mins.x, gl_FragCoord.y * delta.y + mins.y);
	//dvec2 z = dvec2(0, 0);
	dvec2 temp;
	for(int i = 0; i < iters; ++i){
		temp = z;
		//z = dvec2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y) + c;
		z = dvec2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y) + mu;
		if(temp == z){
			break;
		}
		if(z.x * z.x + z.y * z.y >= 4.0 ){ //change back to 4.0
			float pct = float(i)/float(iters);
			fragColor = vec3(pct, pct*pct, pow(2*pct - 1, 2));
			return;
		}
	}
	fragColor = vec3(0, 0, 0);
}