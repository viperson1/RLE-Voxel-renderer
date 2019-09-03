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

    public Renderer(Player player, Level level) {
        this.player = player;
        this.level = level;
    
        this.FOV = (float) Math.PI * 0.5f;
        this.columns = 640;
        this.rows = 360;
    }
    
    public void renderFrame() {
        float recipScreenWidth = 2f / columns;
        float recipScreenHeight = 2f / rows;
        for(int column = 0; column < columns; column++) {
            ArrayList<float[]> constraints = new ArrayList<float[]>();
            constraints.add(new float[] {1, -1});
            
            float horizontalScreenPoint = column * recipScreenWidth;
            float rayDeg = (float)(((horizontalScreenPoint * 0.5f * ((player.direction - (FOV * 0.5f)) - (player.direction + (FOV * 0.5f)))) + (player.direction + (FOV * 0.5f))));

            if(rayDeg < 0) rayDeg += 2*Math.PI;
            if(rayDeg >= 2*Math.PI) rayDeg -= 2*Math.PI;

            Vector2f rayDir = new Vector2f ((float)-Math.sin(rayDeg), (float)-Math.cos(rayDeg));

            Vector2i originSquare = new Vector2i((int)player.position.x, (int)player.position.y);
            Vector2i stepDir = new Vector2i((int)Math.signum(rayDir.x), (int)Math.signum(rayDir.y));

            //length/width of one full grid space
            Vector2f deltaPos = new Vector2f(stepDir.x / rayDir.x, stepDir.y / rayDir.y);

            //how far we need to move to cross a grid boundary
            Vector2f travelMax = new Vector2f(
                    (deltaPos.x > 0 ? deltaPos.x * distToNextInt(originSquare.x) : deltaPos.x * getDecimalPart(originSquare.x)),
                     deltaPos.y > 0 ? deltaPos.y * distToNextInt(originSquare.y) : deltaPos.y * getDecimalPart(originSquare.y));

            Vector2i currentSquare = new Vector2i(originSquare);

            boolean inBounds = true;

            float renderDist = 0f;

            //ector2f point = new Vector2f(player.position.x, player.position.y);

            int side = 0;
            while(inBounds && renderDist < 256) {
                if(travelMax.x < travelMax.y) {
                    renderDist = travelMax.x * (float)Math.cos(player.direction - rayDeg);
                    travelMax.x += deltaPos.x;
                    //currentSquare.x += stepDir.x;
                    //not sure if this works
                    //point.x += deltaPos.x;
                    side = 0;
                }
                else {
                    renderDist = travelMax.y * (float)Math.cos(player.direction - rayDeg);
                    travelMax.y += deltaPos.y;
                    //currentSquare.y += stepDir.y;
                    //not sure if this works
                    //point.y += deltaPos.y;
                    side = 1;
                }

                //renderDist = (float)point.distance(player.position.x, player.position.y) * (float)Math.cos(player.direction - rayDeg);
                
                if(currentSquare.x >= 0 && currentSquare.x < level.getWidth() && currentSquare.y >= 0 && currentSquare.y < level.getHeight()) {
                    int currentHeight = 0;
                    int lastHeight = currentHeight;
                    String slabColumn = level.getLevelArray()[currentSquare.x][currentSquare.y].getColumnString();
                    slabLoop:
                    for(int slabIndex = 0; slabIndex < slabColumn.length(); slabIndex++) {
                        lastHeight = currentHeight;
                        currentHeight += SlabUtils.getLength(slabColumn.charAt(slabIndex));
                        
                        if(SlabUtils.getType(slabColumn.charAt(slabIndex)) != 0) {//the part of the column we are looking at is not air.
                            float botHeightOnScreen = ((1 - (((player.position.z + player.eyeHeight) - (lastHeight)
                            ) / renderDist * heightScale + player.horizon)) * recipScreenHeight);
                            float topHeightOnScreen = ((1 - (((player.position.z + player.eyeHeight) - (currentHeight)
                            ) / renderDist * heightScale + player.horizon)) * recipScreenHeight);
                            
                            for(int i = 0; i < constraints.size(); i++) {
                                float[] constraint = constraints.get(i);
                                if(topHeightOnScreen < constraint[1]) continue;// slabLoop; //now we can be sure this slab will be drawn
                                
                                if(botHeightOnScreen < constraint[1]) botHeightOnScreen = constraint[1]; //will not overdraw on a last slab
                                if(botHeightOnScreen > constraint[0]) continue; //this slab needs to be drawn with different constraints
                                
                                float tempTopHeightOnScreen = topHeightOnScreen; //top height on screen needs to be saved, since we will use it again
                                if(topHeightOnScreen > constraint[0]) tempTopHeightOnScreen = constraint[0];
                                
                                if(botHeightOnScreen > constraint[1] && topHeightOnScreen < constraint[0]) {
                                    //this slab is rendered entirely in this constraint, meaning we will have new top and bottom constraints
                                    
                                    constraints.set(i, new float[]{constraint[0], topHeightOnScreen});
                                    constraints.add(i, new float[]{botHeightOnScreen, constraint[1]});
                                }
                                
                                float color = (float)Math.abs(level.noise.getValue(currentSquare.x / 100f, currentSquare.y / 100f, 1));
    
                                glBegin(GL_QUADS); {
                                    glColor3f(color, color, color);
                                    glVertex2f((float) horizontalScreenPoint - 1, botHeightOnScreen);
                                    glVertex2f((float) horizontalScreenPoint - 1 + recipScreenWidth * 2, botHeightOnScreen);
                                    glVertex2f((float) horizontalScreenPoint - 1 + recipScreenWidth * 2, tempTopHeightOnScreen);
                                    glVertex2f((float) horizontalScreenPoint - 1, tempTopHeightOnScreen);
                                } glEnd();
                                
                                if(constraint[0] == tempTopHeightOnScreen && constraint[1] == botHeightOnScreen) {
                                    constraints.remove(i--);
                                }
                            }
                        }
                    }
                    if(side == 0) currentSquare.x += stepDir.x;
                    else currentSquare.y += stepDir.y;
                }
                else inBounds = false;
            }
            for(float[] constraint : constraints) {
                glBegin(GL_QUADS); {
                    glColor3f(0, 255, 255);
                    glVertex2f((float) horizontalScreenPoint - 1, constraint[1]);
                    glVertex2f((float) horizontalScreenPoint - 1 + recipScreenWidth * 2, constraint[1]);
                    glVertex2f((float) horizontalScreenPoint - 1 + recipScreenWidth * 2, constraint[0]);
                    glVertex2f((float) horizontalScreenPoint - 1, constraint[0]);
                } glEnd();
            }
        }
    }

    private float distToNextInt(float num) {
        return 1 - num + (float)Math.floor(num);
    }

    private float getDecimalPart(float num) {
        return num - (float)Math.floor(num);
    }
}
