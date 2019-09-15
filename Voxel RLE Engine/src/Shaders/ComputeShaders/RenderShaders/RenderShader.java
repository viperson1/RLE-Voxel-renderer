package Shaders.ComputeShaders.RenderShaders;

import Engine.ComputeShader;
import org.lwjgl.BufferUtils;

import java.nio.LongBuffer;
import static org.lwjgl.opengl.GL43.*;

public class RenderShader {
    private ComputeShader shader;
    public RenderShader() {
        shader = new ComputeShader("ComputeShaders/RenderShaders/RLERender.glsl");
    }

    private int createSSBO(long[][][] map, int slabs) {

        LongBuffer mapBuf = BufferUtils.createLongBuffer(map.length * map[0].length * 16);
        for(int x = 0; x < map.length; x++) {
            for(int y = 0; y < map[x].length; y++) {
                for(int z = 0; z < 16; z++) {
                    if(z < map[x][y].length) mapBuf.put(map[x][y][z]);
                    else mapBuf.put(0L);
                }
            }
        }
        int bufferBind = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufferBind);
        glBufferData(GL_SHADER_STORAGE_BUFFER, mapBuf, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        return bufferBind;
    }
}
