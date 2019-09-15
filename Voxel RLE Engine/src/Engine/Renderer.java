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
    public final float heightScale = 180f;
    private final float drawDist = 192f;

    public Renderer(Player player, Level level) {
        this.player = player;
        this.level = level;
    
        this.FOV = (float) Math.PI * 0.5f;
        this.columns = 320;
        this.rows = 180;
    }

    //supporting objects
    private Vector2f rayDir = new Vector2f();
    private Vector2i originSquare = new Vector2i();
    private Vector2i stepDir = new Vector2i();
    private Vector2f deltaPos = new Vector2f();
    private Vector2f travelMax = new Vector2f();
    private Vector2i currentSquare = new Vector2i();
    private Vector2f distToNext = new Vector2f();
    private Vector2f distFromLast = new Vector2f();

    public void renderFrame(int frame) {
        glClearColor(0f, 1f, 1f, 0f);
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        float recipScreenWidth = 2f / columns;
        float recipScreenHeight = 2f / rows;

        distToNext.set(distToNextInt(player.position.x), distToNextInt(player.position.y));
        distFromLast.set(getDecimalPart(player.position.x), getDecimalPart(player.position.y));
        originSquare.set((int)(player.position.x), (int)(player.position.y));

        for(int column = 0; column < columns; column++) {
            float yBufferTop =  1f;
            float yBufferBot = -1f;

            float horizontalScreenPoint = column * recipScreenWidth;
            float rayDeg = (float)(((horizontalScreenPoint * 0.5f * ((player.direction - (FOV * 0.5f)) - (player.direction + (FOV * 0.5f)))) + (player.direction + (FOV * 0.5f))));
            horizontalScreenPoint -= 1;
            
            if(rayDeg < 0) rayDeg += 2*Math.PI;
            if(rayDeg >= 2*Math.PI) rayDeg -= 2*Math.PI;

            rayDir.set((float)-Math.sin(rayDeg), (float)-Math.cos(rayDeg));

            stepDir.set(sign(rayDir.x), sign(rayDir.y));

            //length/width of one full grid space
            deltaPos.set((stepDir.x == 0) ? Float.POSITIVE_INFINITY : stepDir.x / rayDir.x,
                    (stepDir.y == 0) ? Float.POSITIVE_INFINITY : stepDir.y / rayDir.y);

            //how far we need to move to cross a grid boundary
            travelMax.set(
                    (rayDir.x >  0 ? deltaPos.x * distToNext.x : deltaPos.x * distFromLast.x),
                     rayDir.y > 0 ? deltaPos.y * distToNext.y : deltaPos.y * distFromLast.y);

            currentSquare.set(originSquare);

            boolean inBounds = true;

            float lastRenderDist = 0f;
            float renderDist = Math.min(travelMax.x, travelMax.y);

            int side = 0;
            renderLoop:
            while(inBounds && renderDist < drawDist) {
                lastRenderDist = renderDist;
                if (travelMax.x < travelMax.y) {
                    renderDist = travelMax.x * (float) Math.cos(player.direction - rayDeg);
                    travelMax.x += deltaPos.x;
                    side = 0;
                } else {
                    renderDist = travelMax.y * (float) Math.cos(player.direction - rayDeg);
                    travelMax.y += deltaPos.y;
                    side = 1;
                }

                float maxZOnScreen = player.position.z + player.eyeHeight + ((((1 + yBufferTop * (rows * .5f)) - player.horizon) / heightScale) * renderDist);
                float minZOnScreen = player.position.z + player.eyeHeight + ((((1 + yBufferBot * (rows * .5f)) - player.horizon) / heightScale) * renderDist);

                if (currentSquare.x >= 0 && currentSquare.x < level.getWidth() && currentSquare.y >= 0 && currentSquare.y < level.getHeight()) {
                    RLEColumn slabColumn = level.getLevelArray()[level.getIndex(currentSquare.x, currentSquare.y)];

                    int minIndex = slabColumn.getSlabIndex(minZOnScreen);
                    int maxIndex = slabColumn.getSlabIndex(maxZOnScreen);

                    if (minIndex == -1 || maxIndex == -1) {
                        if (side == 0) currentSquare.x += stepDir.x;
                        else currentSquare.y += stepDir.y;

                        continue renderLoop;
                    }

                    for (int slabIndex = maxIndex; slabIndex >= minIndex; slabIndex--) {
                        float heightDiffBot = (player.position.z + player.eyeHeight) - (RLEColumn.getBotHeight(slabColumn.getSlab(slabIndex)));
                        float heightDiffTop = (player.position.z + player.eyeHeight) - (RLEColumn.getTopHeight(slabColumn.getSlab(slabIndex)));

                        float[] botHeightOnScreen = new float[] {((1 - (heightDiffBot) / renderDist * heightScale + player.horizon) * recipScreenHeight)
                                                            , ((1 - (heightDiffBot) / lastRenderDist * heightScale + player.horizon) * recipScreenHeight)};
                        float[] topHeightOnScreen = new float[] {((1 - (heightDiffTop) / renderDist * heightScale + player.horizon) * recipScreenHeight)
                                                        , ((1 - (heightDiffTop) / lastRenderDist * heightScale + player.horizon) * recipScreenHeight)};

                        Color color = new Color(RLEColumn.getColor(slabColumn.getSlab(slabIndex)));

                        Color shadow = color.darker();

                        if (botHeightOnScreen[0] < yBufferBot) {
                            botHeightOnScreen[0] = yBufferBot;
                            yBufferBot = topHeightOnScreen[1];
                            maxZOnScreen = player.position.z + player.eyeHeight + ((((1 - yBufferBot * (rows)) + player.horizon) / heightScale) * renderDist);
                        }
                        if (topHeightOnScreen[1] > yBufferTop) {
                            topHeightOnScreen[1] = yBufferTop;
                            yBufferTop = botHeightOnScreen[0];
                            minZOnScreen = player.position.z + player.eyeHeight + ((((1 - yBufferTop * (rows)) + player.horizon) / heightScale) * renderDist);
                        }
                        if (yBufferBot == yBufferTop) break;

                        //walls
                        glBegin(GL_QUADS);
                            glColor3f(shadow.getRed() / 256f, shadow.getGreen() / 256f, shadow.getBlue() / 256f);
                            glVertex2f(horizontalScreenPoint, topHeightOnScreen[1]);
                            glVertex2f(horizontalScreenPoint + recipScreenWidth * 2, topHeightOnScreen[1]);
                            glVertex2f(horizontalScreenPoint + recipScreenWidth * 2, botHeightOnScreen[1]);
                            glVertex2f(horizontalScreenPoint, botHeightOnScreen[1]);
                        glEnd();

                        //tops
                        glBegin(GL_QUADS);
                        {
                            glColor3f(color.getRed() / 256f, color.getGreen() / 256f, color.getBlue() / 256f);
                            glVertex2f(horizontalScreenPoint, topHeightOnScreen[0]);
                            glVertex2f(horizontalScreenPoint + recipScreenWidth * 2, topHeightOnScreen[0]);
                            glVertex2f(horizontalScreenPoint + recipScreenWidth * 2, botHeightOnScreen[0]);
                            glVertex2f(horizontalScreenPoint, botHeightOnScreen[0]);
                        }
                        glEnd();
                    }
                    if (side == 0) currentSquare.x += stepDir.x;
                    else currentSquare.y += stepDir.y;
                } else inBounds = false;
            }
        }
        glBegin(GL_LINES);
        glVertex2f(-1, player.horizon * recipScreenHeight);
        glVertex2f(1, player.horizon * recipScreenHeight);
        glEnd();

        glBegin(GL_QUADS); {
            glColor3f(0, 1, 1);
            glVertex2f(-1, -1);
            glVertex2f(-1, 1);
            glVertex2f(1, 1);
            glVertex2f(1, -1);
        } glEnd();
    }

    private Vector3i currVoxel = new Vector3i();
    private Vector3f rayCastDir = new Vector3f();
    private Vector3i step3D = new Vector3i();
    private Vector3f tDelta3D = new Vector3f();
    private Vector3f tMax3D = new Vector3f();


    public Vector3i screenRayCast(float distLimit, double rayDeg, float heightOnScreen) {
        rayCastDir.set((float)-Math.sin(rayDeg), (float)-Math.cos(rayDeg), (((1 + heightOnScreen * (rows * .5f)) - player.horizon) / heightScale));
        currVoxel.set((int)player.position.x, (int)player.position.y, (int)(player.position.z + player.eyeHeight));

        step3D.set(sign(rayCastDir.x), sign(rayCastDir.y), sign(rayCastDir.z));

        tDelta3D.set((step3D.x == 0) ? Float.POSITIVE_INFINITY : step3D.x / rayCastDir.x,
                (step3D.y == 0) ? Float.POSITIVE_INFINITY : step3D.y / rayCastDir.y,
                (step3D.z == 0) ? Float.POSITIVE_INFINITY : step3D.z / rayCastDir.z);

        tMax3D.set(step3D.x > 0 ? (tDelta3D.x * distToNextInt(player.position.x)) : (tDelta3D.x * getDecimalPart(player.position.x)),
                step3D.y > 0 ? (tDelta3D.y * distToNextInt(player.position.y)) : (tDelta3D.y * getDecimalPart(player.position.y)),
                step3D.z > 0 ? (tDelta3D.z * distToNextInt(player.position.z + player.eyeHeight)) : (tDelta3D.z * getDecimalPart(player.position.z + player.eyeHeight)));

        float dist = 0f;

        while(dist < distLimit) {
            if(currVoxel.x >= 0 && currVoxel.x < level.getWidth() && currVoxel.y >= 0 && currVoxel.y < level.getWidth()) {
                //we are in the bounds of the map
                if(level.getLevelArray()[level.getIndex(currVoxel.x, currVoxel.y)].getSlab((float)currVoxel.z) != -1) {
                    return currVoxel;
                }
            } else break;
            if(tMax3D.x < tMax3D.y) {
                if(tMax3D.x < tMax3D.z) {
                    //x is minimum
                    currVoxel.x += step3D.x;
                    dist = tMax3D.x;
                    tMax3D.x += tDelta3D.x;
                }
                else {
                    //z is minimum
                    currVoxel.z += step3D.z;
                    dist = tMax3D.z;
                    tMax3D.z += tDelta3D.z;
                }
            }
            else {
                if(tMax3D.y < tMax3D.z) {
                    //y is minimum
                    currVoxel.y += step3D.y;
                    dist = tMax3D.y;
                    tMax3D.y += tDelta3D.y;
                }
                else {
                    //z is minimum
                    currVoxel.z += step3D.z;
                    dist = tMax3D.y;
                    tMax3D.z += tDelta3D.z;
                }
            }
        }

        return null;
    }

    private float distToNextInt(float num) {
        return (float)(Math.ceil(num) - num);
    }

    private float getDecimalPart(float num) {
        return (float)(num - Math.floor(num));
    }
    
    private int sign(float num) {
        return (num > 0) ? 1 : (num == 0) ? 0 : -1;
    }
}
