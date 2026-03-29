package environment;
import org.joml.Vector3f;

public class DayNightCycle {

    private float time = 0.25f; // Start at midday

    private static final float CYCLE_LENGTH = 900f; // 15 minutes total
    private static final float DAY_FRACTION = 0.667f; // 10 min day, 5 min night

    public void update(float deltaTime) {
        time += deltaTime / CYCLE_LENGTH;
        if (time > 1f) time -= 1f;
    }

    private float getSunHeight() {
        if (time < DAY_FRACTION) {
            float t = time / DAY_FRACTION;
            return (float) Math.sin(t * Math.PI);
        } else {
            float t = (time - DAY_FRACTION) / (1f - DAY_FRACTION);
            return (float) Math.sin(Math.PI + t * Math.PI);
        }
    }

    private float smoothstep(float edge0, float edge1, float x) {
        x = Math.max(0f, Math.min(1f, (x - edge0) / (edge1 - edge0)));
        return x * x * (3f - 2f * x);
    }

    private Vector3f lerp(Vector3f a, Vector3f b, float t) {
        return new Vector3f(
            a.x + (b.x - a.x) * t,
            a.y + (b.y - a.y) * t,
            a.z + (b.z - a.z) * t
        );
    }

    public Vector3f getLightDir() {
        float t = time / DAY_FRACTION;
        float angle = (float) (t * Math.PI);
        return new Vector3f(
            (float) Math.cos(angle),
            (float) Math.sin(angle),
            0.3f
        ).normalize();
    }

    public float getAmbient() {
        float sun = getSunHeight();

        float night = 0.22f;
        float day = 0.85f;

        float t = smoothstep(-0.18f, 0.25f, sun);
        return night + (day - night) * t;
    }

    public Vector3f getLightColor() {
        float sun = getSunHeight();

        Vector3f nightColor = new Vector3f(0.55f, 0.60f, 0.85f);
        Vector3f sunsetColor = new Vector3f(1.00f, 0.65f, 0.35f);
        Vector3f dayColor = new Vector3f(1.00f, 1.00f, 1.00f);

        // wide smooth transition across EVERYTHING
        float t = smoothstep(-0.6f, 0.8f, sun);

        // shape the curve (this is the sauce)
        t = t * t * (3f - 2f * t); // extra smooth

        // blend night → sunset → day in one chain
        Vector3f mid = lerp(nightColor, sunsetColor, t);
        return lerp(mid, dayColor, t);
    }

    public Vector3f getSkyColor() {
        float sun = getSunHeight();

        Vector3f nightSky = new Vector3f(0.03f, 0.04f, 0.10f);
        Vector3f sunriseSky = new Vector3f(0.95f, 0.45f, 0.20f);
        Vector3f daySky = new Vector3f(0.40f, 0.65f, 1.00f);

        if (sun <= 0f) {
            float t = smoothstep(-0.35f, 0.08f, sun);
            return lerp(nightSky, sunriseSky, t);
        } else {
            float t = smoothstep(0.0f, 0.7f, sun);
            return lerp(sunriseSky, daySky, t);
        }
    }

    public boolean isDay() {
        return getSunHeight() > 0f;
    }
}