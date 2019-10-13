__kernel void(__constant long** level, __constant float5 position, __constant float FOV) {
    static float PI = 3.1415927f;

    int column = get_global_id(0);
    float rayDeg = mix(position.s3 - (FOV * .5f), position.s3 + (FOV * .5f), column / convert_float4_rtp( get_num_groups ));
    float2 rayDir = { -sin(rayDeg), -cos(rayDeg) };
    clamp(rayDir.x, 0, 2*PI);
    clamp(rayDir.y, 0, 2*PI);

    int2 step = sign(rayDir);

    float2 deltaPos = step * rcp(rayDir);

    int2 currentSquare = floor(position.xy);

    float2 travelMax = abs((currentSquare + max(step, 0.0) - position.xy)) * rcp(rayDir);

    float renderDist = 0.0f;
    bool side = true;

    int yBuffer = 0;
}