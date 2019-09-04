package Engine;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL.*;

import Entity.Player;
import Map.Level;
import Map.SlabUtils;
import com.flowpowered.noise.Noise;
import com.flowpowered.noise.module.source.Perlin;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class Renderer {
    Player player;
    Level level;

    int columns, rows;
    float FOV;
    private final float heightScale = 192;
    private final float drawDist = 512f;
    
    public Renderer(Player player, Level level) {
        this.player = player;
        this.level = level;
    
        this.FOV = (float) Math.PI * 0.5f;
        this.columns = 360;
        this.rows = 180;
    }
    
    public void renderFrame() {
        glClearColor(0, 1, 1, 0);
        glClear(GL_COLOR_BUFFER_BIT);
        float recipScreenWidth = 2f / columns;
        float recipScreenHeight = 2f / rows;
        for(int column = 0; column < columns; column++) {
            boolean[] columnMask = new boolean[rows];
            
            float horizontalScreenPoint = column * recipScreenWidth;
            float rayDeg = (float)(((horizontalScreenPoint * 0.5f * ((player.direction - (FOV * 0.5f)) - (player.direction + (FOV * 0.5f)))) + (player.direction + (FOV * 0.5f))));
            horizontalScreenPoint -= 1;
            
            if(rayDeg < 0) rayDeg += 2*Math.PI;
            if(rayDeg >= 2*Math.PI) rayDeg -= 2*Math.PI;

            Vector2f rayDir = new Vector2f ((float)-Math.sin(rayDeg), (float)-Math.cos(rayDeg));

            Vector2i originSquare = new Vector2i((int)player.position.x, (int)player.position.y);
            Vector2i stepDir = new Vector2i((int)sign(rayDir.x), (int)sign(rayDir.y));

            //length/width of one full grid space
            Vector2f deltaPos = new Vector2f(stepDir.x / rayDir.x, stepDir.y / rayDir.y);

            //how far we need to move to cross a grid boundary
            Vector2f travelMax = new Vector2f(
                    (deltaPos.x > 0 ? deltaPos.x * distToNextInt(originSquare.x) : deltaPos.x * getDecimalPart(originSquare.x)),
                     deltaPos.y > 0 ? deltaPos.y * distToNextInt(originSquare.y) : deltaPos.y * getDecimalPart(originSquare.y));

            Vector2i currentSquare = new Vector2i(originSquare);

            boolean inBounds = true;

            float renderDist = 0f;

            int side = 0;
            while(inBounds && renderDist < drawDist) {
                if (travelMax.x < travelMax.y) {
                    renderDist = travelMax.x * (float) Math.cos(player.direction - rayDeg);
                    travelMax.x += deltaPos.x;
                    side = 0;
                } else {
                    renderDist = travelMax.y * (float) Math.cos(player.direction - rayDeg);
                    travelMax.y += deltaPos.y;
                    side = 1;
                }
    
                if (currentSquare.x >= 0 && currentSquare.x < level.getWidth() && currentSquare.y >= 0 && currentSquare.y < level.getHeight()) {
                    int currentHeight = 0;
                    int lastHeight = currentHeight;
                    String slabColumn = level.getLevelArray()[currentSquare.x][currentSquare.y].getColumnString();
                    
                    int columnLength = slabColumn.length();
                    for (int slabIndex = 0; slabIndex < columnLength; slabIndex++) {
                        lastHeight = currentHeight;
                        currentHeight += SlabUtils.getLength(slabColumn.charAt(slabIndex));
            
                        if (SlabUtils.getType(slabColumn.charAt(slabIndex)) != 0) {//the part of the column we are looking at is not air.
                            float topHeightOnScreen = ((1 - (((player.position.z + player.eyeHeight) - (currentHeight)
                            ) / renderDist * heightScale + player.horizon)) * recipScreenHeight);
                            float botHeightOnScreen = ((1 - (((player.position.z + player.eyeHeight) - (lastHeight)
                            ) / renderDist * heightScale + player.horizon)) * recipScreenHeight);
    
    
                            float color = renderDist / 256f;//(float) Math.abs(level.noise.getValue(currentSquare.x / 100f, currentSquare.y / 100f, 1));
    
                            for (float i = Math.max(botHeightOnScreen, -1); i < Math.min(topHeightOnScreen, 1); i += recipScreenHeight) {
                                if (!columnMask[(int) ((i * rows * .5) + (rows / 2))]) {
                                    glBegin(GL_QUADS);{
                                        glColor3f(color, color, color);
                                        glVertex2f(horizontalScreenPoint, i);
                                        glVertex2f(horizontalScreenPoint + recipScreenWidth * 2, i);
                                        glVertex2f(horizontalScreenPoint + recipScreenWidth * 2, i + recipScreenHeight * 2);
                                        glVertex2f(horizontalScreenPoint, i + recipScreenHeight * 2);
                                    }glEnd();
                                    columnMask[(int) ((i * rows * .5) + (rows / 2))] = true;
                                }
                            }
                        }
                    }
                    if (side == 0) currentSquare.x += stepDir.x;
                    else currentSquare.y += stepDir.y;
                } else inBounds = false;
            }
        }
    }

    private float distToNextInt(float num) {
        return 1 - num + (float)Math.floor(num);
    }

    private float getDecimalPart(float num) {
        return num - (float)Math.floor(num);
    }
    
    private int sign(float num) {
        return (num > 0) ? 1 : -1;
    }
}
