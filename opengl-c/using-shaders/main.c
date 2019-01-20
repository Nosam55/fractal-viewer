#include<stdio.h>
#include<stdlib.h>
#include<GL/glew.h>
#include<GLFW/glfw3.h>

#define WIDTH 1200
#define HEIGHT 900

typedef struct {
	double x;
	double y;
} vec2;

static const float clipCoords[] = {
	-1.0f, 1.0f,
	1.0f, -1.0f,
	-1.0f, -1.0f,

	-1.0f, 1.0f,
	1.0f, 1.0f,
	1.0f, -1.0f
};

static int iters = 50;

static double xmin = -2.0f,
	xmax = 1.0f,
	ymin = -1.0f,
	ymax = 1.0f;

static int isPaused = 0;

static struct {
	GLuint position;
	GLuint delta;
	GLuint mins;
	GLuint iters;
} layouts;


inline vec2 get_delta() {
	return (vec2) { (xmax - xmin) / WIDTH, (ymax - ymin) / HEIGHT };
}
void check_gl_errors() {
	switch (glGetError()) {
	case GL_INVALID_ENUM:
		fputs("error: invalid enum!", stderr);
		break;
	case GL_INVALID_VALUE:
		fputs("error: invalid value!", stderr);
		break;
	case GL_INVALID_OPERATION:
		fputs("error: invalid operation!", stderr);
		break;
	case GL_INVALID_FRAMEBUFFER_OPERATION:
		fputs("error: invalid framebuffer operation!", stderr);
		break;
	case GL_OUT_OF_MEMORY:
		fputs("error: out of memory!", stderr);
		break;
	case GL_STACK_OVERFLOW:
		fputs("error: stack overflow!", stderr);
		break;
	case GL_STACK_UNDERFLOW:
		fputs("error: stack underflow!", stderr);
	}
}
void on_key_press(GLFWwindow *window, int key, int scancode, int action, int mods) {
	if (key == GLFW_KEY_LEFT_SHIFT || key == GLFW_KEY_RIGHT_SHIFT)
		return;
	if (action == GLFW_PRESS) {
		switch (key) {
		case GLFW_KEY_RIGHT:
			iters += (mods == GLFW_MOD_SHIFT ? 500 : 50);
			glUniform1i(layouts.iters, iters);
			printf("%i iterations per pixel\n", iters);
			break;
		case GLFW_KEY_LEFT:
			iters -= (mods == GLFW_MOD_SHIFT ? 500 : 50);
			glUniform1i(layouts.iters, iters);
			printf("%i iterations per pixel\n", iters);
			break;
		case GLFW_KEY_SPACE:
			//TODO: REDRAW
			printf("Centered at %.15f %+.15fi\n", (xmax - xmin) / 2.0 + xmin, (ymax - ymin) / 2.0 + ymin);
			break;
		default:
			printf("(Shift +)Right/Left Arrow: Increase/Decrease iters by 50 (500 w/ shift)\nSpace: Redraw the screen\n");
			break;
		}
	}
	isPaused = 0;
}
void center_on_point(const vec2 center) {
	vec2 diff;
	diff.x = xmax - xmin;
	diff.y = ymax - ymin;
	xmin = center.x - diff.x / 2;
	xmax = center.x + diff.x / 2;
	ymin = center.y - diff.y / 2;
	ymax = center.y + diff.y / 2;
}

void on_click(GLFWwindow *window, int button, int action, int mods) {
	if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
		vec2 pos, newcenter, delta = get_delta(), diff;
		double tempx, tempy;
		glfwGetCursorPos(window, &tempx, &tempy);
		pos.x = tempx;
		pos.y = tempy;
		diff.x = xmax - xmin;
		diff.y = ymax - ymin;
		newcenter.x = delta.x * pos.x + xmin;
		newcenter.y = delta.y * (HEIGHT - pos.y) + ymin;
		center_on_point(newcenter);
		printf("Centered at %.15f %+.15fi\n", newcenter.x, newcenter.y);
	}
	else if (button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS) {
		xmin = -2.0, xmax = 1.0, ymin = -1.0, ymax = 1.0;
		printf("Centered at %.15f %+.15fi\n", (xmax - xmin) / 2.0 + xmin, (ymax - ymin) / 2.0 + ymin);
		glUniform2d(layouts.delta, get_delta().x, get_delta().y);
	}
	glUniform2d(layouts.mins, xmin, ymin);
	isPaused = 0;
}
void on_scroll(GLFWwindow *window, double xoffset, double yoffset) {
	vec2 diff;
	diff.x = xmax - xmin;
	diff.y = ymax - ymin;
	if (yoffset > 0.0) {
		xmin += diff.x / 4;
		xmax -= diff.x / 4;
		ymin += diff.y / 4;
		ymax -= diff.y / 4;

	}
	else if (yoffset < 0.0) {
		xmin -= diff.x / 4;
		xmax += diff.x / 4;
		ymin -= diff.y / 4;
		ymax += diff.y / 4;

	}
	glUniform2d(layouts.mins, xmin, ymin);
	glUniform2d(layouts.delta, get_delta().x, get_delta().y);
	isPaused = 0;
}
GLuint load_shaders(const char *vertex_path, const char *frag_path) {
	GLuint vertexID = glCreateShader(GL_VERTEX_SHADER),
		fragID = glCreateShader(GL_FRAGMENT_SHADER),
		programID = glCreateProgram();
	GLint result = GL_FALSE, logLength;
	char vbuff[2048] = { 0 }, fbuff[2048] = { 0 }, in;
	FILE *shader = fopen(vertex_path, "r");
	int count = 0;

	//Load the shader code
	while ((in = fgetc(shader)) != EOF) {
		vbuff[count++] = in;
		if (count >= 2048) {
			fprintf(stderr, "vertex shader is too long!\n");
			break;
		}
	}
	count = 0;
	fclose(shader);

	shader = fopen(frag_path, "r");
	while ((in = fgetc(shader)) != EOF) {
		fbuff[count++] = in;
		if (count >= 2048) {
			fprintf(stderr, "fagment shader is too long!\n");
			break;
		}
	}
	fclose(shader);

	//Compile the vertex shader
	const char *vcode = vbuff, *fcode = fbuff;
	printf("Compiling shader: %s\n", vertex_path);
	glShaderSource(vertexID, 1, &vcode, NULL);
	glCompileShader(vertexID);

	glGetShaderiv(vertexID, GL_COMPILE_STATUS, &result);
	glGetShaderiv(vertexID, GL_INFO_LOG_LENGTH, &logLength);
	if (logLength > 0) {
		char *err = calloc(logLength + 1, sizeof(char));
		glGetShaderInfoLog(vertexID, logLength, NULL, err);
		printf("%s\n", err);
		free(err);
	}
	printf("%s\n", vbuff);
	//Compile the fragment shader
	printf("Compiling shader: %s\n", frag_path);
	glShaderSource(fragID, 1, &fcode, NULL);
	glCompileShader(fragID);

	glGetShaderiv(fragID, GL_COMPILE_STATUS, &result);
	glGetShaderiv(fragID, GL_INFO_LOG_LENGTH, &logLength);
	if (logLength > 0) {
		char *err = calloc(logLength + 1, sizeof(char));
		glGetShaderInfoLog(fragID, logLength, NULL, err);
		printf("%s\n", err);
		free(err);
	}
	printf("%s\n", fbuff);
	//Link the program
	printf("Linking\n");
	glAttachShader(programID, vertexID);
	glAttachShader(programID, fragID);
	glLinkProgram(programID);

	glGetProgramiv(programID, GL_LINK_STATUS, &result);
	glGetProgramiv(programID, GL_INFO_LOG_LENGTH, &logLength);
	if (logLength > 0) {
		char *err = calloc(logLength + 1, sizeof(char));
		glGetProgramInfoLog(programID, logLength, NULL, err);
		printf("%s\n", err);
		free(err);
	}

	//Cleanup
	glDetachShader(programID, vertexID);
	glDetachShader(programID, fragID);

	glDeleteShader(vertexID);
	glDeleteShader(fragID);

	return programID;
}


int main(void) {
	GLFWwindow *window;
	GLuint program, vertexArray, vertexBuffer;

	if (glfwInit() != GLFW_TRUE) {
		fprintf(stderr, "unable to initialize glfw!\n");
		return EXIT_FAILURE;
	}

	glfwWindowHint(GLFW_SAMPLES, 4);
	glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 4);
	glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

	window = glfwCreateWindow(WIDTH, HEIGHT, "shader boye", NULL, NULL);

	if (window == NULL) {
		fprintf(stderr, "unable to initialize OpenGL 4.4!\n");
		glfwTerminate();
		return EXIT_FAILURE;
	}

	glfwMakeContextCurrent(window);
	glewExperimental = GL_TRUE;

	if (glewInit() != GLEW_OK) {
		fprintf(stderr, "unable to initialize glew!\n");
		glfwTerminate();
		return EXIT_FAILURE;
	}
	//setup controls
	glfwSetKeyCallback(window, on_key_press);
	glfwSetMouseButtonCallback(window, on_click);
	glfwSetScrollCallback(window, on_scroll);
	//Setup data
	glGenVertexArrays(1, &vertexArray);
	glBindVertexArray(vertexArray);

	glGenBuffers(1, &vertexBuffer);
	glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
	glBufferData(GL_ARRAY_BUFFER, sizeof(clipCoords), clipCoords, GL_STATIC_DRAW);
	//Setup shaders
	program = load_shaders("vertex.glsl", "fragment.glsl");
	layouts.position = glGetAttribLocation(program, "position");
	layouts.delta = glGetUniformLocation(program, "delta");
	layouts.mins = glGetUniformLocation(program, "mins");
	layouts.iters = glGetUniformLocation(program, "iters");
	glUseProgram(program);
	printf("delta: %i\nmins: %i\niters: %i\n", layouts.delta, layouts.mins, layouts.iters);
	glUniform2d(layouts.delta, get_delta().x, get_delta().y);
	glUniform2d(layouts.mins, xmin, ymin);
	glUniform1i(layouts.iters, iters);
	
	///////////////
	while (!glfwWindowShouldClose(window)) {
		if (isPaused < 2) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			glUseProgram(program);
			//puts("lop");

			glEnableVertexAttribArray(layouts.position);
			glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
			glVertexAttribPointer(layouts.position, 2, GL_FLOAT, 0, 0, NULL);
			glDrawArrays(GL_TRIANGLES, 0, 6);
			glDisableVertexAttribArray(0);
			glfwSwapBuffers(window);
			isPaused++;
		}
		glfwPollEvents();
	}

	return EXIT_SUCCESS;
}