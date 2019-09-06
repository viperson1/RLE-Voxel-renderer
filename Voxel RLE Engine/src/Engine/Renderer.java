package Engine;

import static org.lwjgl.opengl.GL11.*;

import Entity.Player;
import Map.Level;
import Map.RLEColumn;
import org.joml.*;

import java.awt.*;
import java.lang.Math;

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
        this.columns = 320;
        this.rows = 240;
    }
    
    public void renderFrame() {
        glClearColor(0, 1, 1, 0);
        glClear(GL_COLOR_BUFFER_BIT);
        float recipScreenWidth = 2f / columns;
        float recipScreenHeight = 2f / rows;
        for(int column = 0; column < columns; column++) {
            short[] columnMask = new short[rows];
            for(int i = 0; i < rows; i++) columnMask[i] = -1;
            
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
    
                float maxZOnScreen = player.position.z + player.eyeHeight + ((((1 + (rows)) + player.horizon) / heightScale) * renderDist);
                float minZOnScreen = player.position.z + player.eyeHeight + ((((1 - (rows)) + player.horizon) / heightScale) * renderDist);
                
                if (currentSquare.x >= 0 && currentSquare.x < level.getWidth() && currentSquare.y >= 0 && currentSquare.y < level.getHeight()) {
                    int currentHeight = 0;
                    int lastHeight;
                    RLEColumn slabColumn = level.getLevelArray()[currentSquare.x][currentSquare.y];
                    
                    for (int slabIndex = slabColumn.getSlabIndex(minZOnScreen); slabIndex < slabColumn.getSlabIndex(maxZOnScreen); slabIndex++) {
                        float heightDiffBot = (player.position.z + player.eyeHeight) - (RLEColumn.getBotHeight(slabColumn.getSlab(slabIndex)));
                        float heightDiffTop = (player.position.z + player.eyeHeight) - (RLEColumn.getTopHeight(slabColumn.getSlab(slabIndex)));
                        
                        float botHeightOnScreen = ((1 - (heightDiffBot) / renderDist * heightScale + player.horizon) * recipScreenHeight);
                        float topHeightOnScreen = ((1 - (heightDiffTop) / renderDist * heightScale + player.horizon) * recipScreenHeight);

                        

                        Color color = new Color(RLEColumn.getColor(slabColumn.getSlab(slabIndex)));
                        
                        if(side == 0) color.darker();
                        
                        float bot =  Math.max(botHeightOnScreen, -1);
                        float top = Math.min(topHeightOnScreen, 1);
                        glBegin(GL_QUADS); {
                            glColor3f(color.getRed() / 256f, color.getGreen() / 256f, color.getBlue() / 256f);
                            glVertex2f(horizontalScreenPoint, bot);
                            glVertex2f(horizontalScreenPoint + recipScreenWidth * 2, bot);
                            glVertex2f(horizontalScreenPoint + recipScreenWidth * 2, top);
                            glVertex2f(horizontalScreenPoint, top);
                        } glEnd();
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
