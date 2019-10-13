package Engine.GPUCompute;

import Engine.Renderer;
import Entity.Player;
import Map.Level;
import org.jocl.*;

import static org.jocl.CL.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CLRenderManager {
    Level level;
    Player player;
    Renderer renderer;

    Pointer[] renderLevel;

    final int platformIndex = 0;
    final long deviceType = CL_DEVICE_TYPE_ALL;
    final int deviceIndex = 0;

    int numPlatforms;

    cl_platform_id platform;
    cl_context_properties contextProperties;
    cl_device_id device;

    int numDevices;

    cl_context context;
    cl_command_queue commandQueue;
    cl_mem[] memObjects;
    cl_program program;
    cl_kernel kernel;

    long[] global_work_size;
    long[] local_work_size;

    public CLRenderManager(Level level, Player player, Renderer renderer) {

        this.level = level;
        this.player = player;
        this.renderer = renderer;
        renderLevel = new Pointer[level.getWidth() * level.getHeight()];
        long numValues = levelToRenderData();

        CL.setExceptionsEnabled(true);

        int[] numPlatformsArray = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        numPlatforms = numPlatformsArray[0];

        cl_platform_id[] platforms = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        platform = platforms[platformIndex];

        contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        int[] numDevicesArray = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        numDevices = numDevicesArray[0];

        cl_device_id[] devices = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        device = devices[0];

        context = clCreateContext(contextProperties, 1, new cl_device_id[] {device}, null, null, null);
        commandQueue = clCreateCommandQueue(context, device, 0, null);

        Pointer renderLevelPointer = Pointer.to(renderLevel);
        Pointer playerPositionPointer = Pointer.to(new float[] {player.position.x, player.position.y, player.position.z + player.horizon, (float)player.direction, player.horizon});
        Pointer FOV = Pointer.to(new float[] {renderer.FOV});

        memObjects = new cl_mem[3];
        memObjects[0] =  clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_long * numValues, renderLevelPointer, null);
        memObjects[1] = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * 5, playerPositionPointer, null);
        memObjects[2] = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float, FOV, null);

        program = clCreateProgramWithSource(context, 0, new String[]{readFile("render.cl")}, null, null);
        clBuildProgram(program, 0, null, null, null, null);

        kernel = clCreateKernel(program, "renderKernel", null);
    }

    private String readFile(String filePath) {
        StringBuilder string = new StringBuilder();
        BufferedReader br;
        try {
            String line;
            br = new BufferedReader(new FileReader("Voxel RLE Engine\\src\\Engine\\GPUCompute\\Kernels\\" + filePath));
            while((line = br.readLine()) != null) {
                string.append(line + "\n");
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return string.toString();
    }

    public void releaseMem() {
        for(cl_mem memObj : memObjects) {
            clReleaseMemObject(memObj);
        }
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
    }

    public long levelToRenderData() {
        long numValues = 0;
        for(int x = 0; x < level.getWidth(); x++) {
            for(int y = 0; y < level.getHeight(); y++) {
                Object[] column = level.getLevelArray()[level.getIndex(x, y)].getColumnString().toArray();

                long[] primColumn = new long[column.length];
                for(int i = 0; i < column.length; i++) {
                    primColumn[i] = (Long)column[i]; numValues++;
                }

                renderLevel[(y * level.getWidth()) + x] = Pointer.to(primColumn);
            }
        }
        return numValues;
    }
}
