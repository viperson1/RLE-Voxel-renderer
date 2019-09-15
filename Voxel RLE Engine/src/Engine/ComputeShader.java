package Engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.lwjgl.opengl.GL43.*;

public class ComputeShader {
    private int program;
    private int shader;

    public ComputeShader(String filePath) {
        program = glCreateProgram();

        shader = glCreateShader(GL_COMPUTE_SHADER);
        glShaderSource(shader, readFile(filePath));
        glCompileShader(shader);
        if(glGetShaderi(shader, GL_COMPILE_STATUS) != 1) {
            System.out.println(glGetShaderInfoLog(shader));
        }

        glAttachShader(program, shader);
    }

    private String readFile(String filePath) {
        StringBuilder string = new StringBuilder();
        BufferedReader br;
        try {
            String line;
            br = new BufferedReader(new FileReader("Voxel RLE Engine\\src\\Shaders" + filePath));
            while((line = br.readLine()) != null) {
                string.append(line + "\n");
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return string.toString();
    }
}
