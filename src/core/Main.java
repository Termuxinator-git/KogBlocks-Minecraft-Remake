package core;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_T;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_COMPAT_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLoadMatrixf;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform1i;


import java.util.Iterator;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;

import blocks.BlockBreaker;
import blocks.BlockDrops;
import blocks.BlockOutline;
import blocks.BlockTypes;
import commands.CommandProcessor;
import controls.Camera;
import controls.Crosshair;
import controls.SteveRenderer;
import entity.ItemEntity;
import entity.Player;
import environment.DayNightCycle;
import environment.SkyRenderer;
import gui.ChatOverlay;
import gui.CraftingTableScreen;
import inventory.Hotbar;
import inventory.Inventory;
import inventory.InventoryScreen;
import items.DroppedItemRenderer;
import items.ItemRegistry;
import items.ItemTypes;
import render.FontRenderer;
import render.Shader;
import render.TextureManager;
import world.Chunk;
import world.World;

public class Main {
    long window;

    Camera camera = new Camera();
    Shader shader;
    World world = new World();
    DayNightCycle dayNight = new DayNightCycle();
    Crosshair crosshair = new Crosshair();
    BlockOutline blockOutline = new BlockOutline();
    SkyRenderer skyRenderer = new SkyRenderer();

    Inventory playerInventory = new Inventory();
    Hotbar hotbar = new Hotbar(playerInventory);
    InventoryScreen inventory = new InventoryScreen();
    CraftingTableScreen craftingTableScreen = new CraftingTableScreen();

    BlockBreaker blockBreaker = new BlockBreaker();
    DroppedItemRenderer droppedItemRenderer = new DroppedItemRenderer();
    SteveRenderer steveRenderer = new SteveRenderer();

    ChatOverlay chatOverlay = new ChatOverlay();
    FontRenderer chatFont = new FontRenderer();

    Matrix4f projection;
    float currentFov = 70f;
    float lastFrame = 0f;

    Player player;

    boolean inventoryOpen = false;
    boolean craftingTableOpen = false;
    boolean eWasDown = false;
    boolean qWasDown = false;
    boolean tWasDown = false;

    public void run() {
        init();
        loop();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    void init() {
        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(1024, 768, "KogBlocks", NULL, NULL);
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        Input.init(window);
    }

    void loop() {
        GL.createCapabilities();

        TextureManager.init();
        crosshair.init();
        skyRenderer.init();
        blockOutline.init();
        blockBreaker.init();
        hotbar.init();
        inventory.init();
        craftingTableScreen.init();
        steveRenderer.init();
        inventory.setSteveRenderer(steveRenderer);
        ItemRegistry.init();

        glEnable(GL_DEPTH_TEST);
        glClearColor(0.5f, 0.8f, 1.0f, 0.0f);

        shader = new Shader("shaders/vertex.glsl", "shaders/fragment.glsl");

        projection = new Matrix4f().perspective(
            (float) Math.toRadians(currentFov), 1024f / 768f, 0.1f, 1000f
        );

        world.update(8, 8);
        player = new Player(world);
        lastFrame = (float) glfwGetTime();

        while (!glfwWindowShouldClose(window)) {
            float currentFrame = (float) glfwGetTime();
            float deltaTime = currentFrame - lastFrame;
            lastFrame = currentFrame;

            if (deltaTime > 0.05f) {
                deltaTime = 0.05f;
            }

            boolean gameInputBlocked = inventoryOpen || craftingTableOpen || chatOverlay.isOpen();

            boolean tDown = Input.isKeyDown(GLFW_KEY_T);
            if (tDown && !tWasDown && !inventoryOpen && !craftingTableOpen && !chatOverlay.isOpen()) {
                chatOverlay.open();
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                Input.resetDeltas();
            }
            tWasDown = tDown;

            String submitted = chatOverlay.pollSubmittedText(glfwGetTime(), deltaTime);
            if (submitted != null) {
                String result = CommandProcessor.execute(submitted, playerInventory, world, player);

                if (submitted.startsWith("/")) {
                    if (result != null && !result.isEmpty()) {
                        chatOverlay.addMessage(result, glfwGetTime());
                    }
                } else {
                    chatOverlay.addMessage(submitted, glfwGetTime());
                }

                if (!inventoryOpen && !craftingTableOpen && !chatOverlay.isOpen()) {
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                    Input.resetDeltas();
                }
            }

            boolean eDown = Input.isKeyDown(GLFW_KEY_E);
            if (!chatOverlay.isOpen() && eDown && !eWasDown) {
                if (craftingTableOpen) {
                    craftingTableOpen = false;
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                    Input.resetDeltas();
                } else {
                    inventoryOpen = !inventoryOpen;

                    if (inventoryOpen) {
                        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                    } else {
                        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                        Input.resetDeltas();
                    }
                }
            }
            eWasDown = eDown;

            boolean qDown = Input.isKeyDown(GLFW_KEY_Q);
            if (!gameInputBlocked && qDown && !qWasDown) {
                dropSelectedItem();
            }
            qWasDown = qDown;

            world.update(player.position.x, player.position.z);

            if (!gameInputBlocked) {
                player.update(deltaTime, camera, world);
            }

            if (!gameInputBlocked) {
                hotbar.handleInput();
            } else if (craftingTableOpen) {
                craftingTableScreen.handleInput(hotbar);
            } else if (inventoryOpen) {
                inventory.handleInput(hotbar);
            }

            world.updateItems(deltaTime);
            updateItemPickup(deltaTime);

            float targetFov = player.isSprinting ? 85f : 70f;
            currentFov += (targetFov - currentFov) * 0.1f;
            projection = new Matrix4f().perspective(
                (float) Math.toRadians(currentFov), 1024f / 768f, 0.1f, 1000f
            );

            dayNight.update(deltaTime);
            Vector3f sky = dayNight.getSkyColor();
            glClearColor(sky.x, sky.y, sky.z, 1f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glEnable(GL_DEPTH_TEST);
            glDisable(GL_BLEND);
            glColor4f(1f, 1f, 1f, 1f);

            shader.use();
            glActiveTexture(GL_TEXTURE0);
            glUniform1i(glGetUniformLocation(shader.getProgramId(), "tex"), 0);
            
            shader.setFloat("ambientStrength", dayNight.getAmbient());
            shader.setVec3("lightColor", dayNight.getLightColor());
            shader.setMatrix4f("view", camera.getViewMatrix());
            shader.setMatrix4f("projection", projection);
            shader.setVec3("fogColor", sky);
            shader.setFloat("fogStart", 32f);
            shader.setFloat("fogEnd", 86f);

            world.render(shader);

            glUseProgram(0);

            glMatrixMode(GL_PROJECTION);
            glLoadMatrixf(projection.get(new float[16]));

            glMatrixMode(GL_MODELVIEW);
            glLoadMatrixf(camera.getViewMatrix().get(new float[16]));

            for (ItemEntity item : world.getItemEntities()) {
                droppedItemRenderer.render(item, deltaTime);
            }

            skyRenderer.render(camera.position, dayNight, camera.getViewMatrix(), projection);

            Vector3f camFront = camera.getFront();
            int[] targeted = world.raycast(camera.position, camFront, 6f);

            blockOutline.render(targeted, camera, projection);

            boolean brokeBlock = false;
            if (!gameInputBlocked && targeted != null) {
                brokeBlock = blockBreaker.update(deltaTime, targeted, world);
            } else {
                blockBreaker.update(deltaTime, null, world);
            }

            steveRenderer.setBreaking(!gameInputBlocked && blockBreaker.isBreaking());
            steveRenderer.updateSwing(deltaTime);

            if (!gameInputBlocked &&
                Input.isMouseJustPressed(GLFW_MOUSE_BUTTON_LEFT) &&
                targeted == null) {
                steveRenderer.startPunch();
            }

            if (brokeBlock) {
                int bx = targeted[0];
                int by = targeted[1];
                int bz = targeted[2];

                int brokenBlock = world.getBlock(bx, by, bz);
                world.setBlock(bx, by, bz, BlockTypes.AIR);

                int dropItem = BlockDrops.getDrop(brokenBlock);

                if (dropItem != BlockTypes.AIR) {
                    world.spawnItem(dropItem, 1, bx + 0.5f, by + 0.3f, bz + 0.5f);
                }
            }

            blockBreaker.render(targeted, camera, projection);
            glUseProgram(0);
            glBindTexture(GL_TEXTURE_2D, 0);
            glColor4f(1f, 1f, 1f, 1f);
            glDisable(GL_BLEND);
            glEnable(GL_DEPTH_TEST);

            if (!inventoryOpen && !craftingTableOpen) {
                glClear(GL_DEPTH_BUFFER_BIT);
                steveRenderer.renderFirstPersonArm(camera, player, hotbar);
                glUseProgram(0);
                glBindTexture(GL_TEXTURE_2D, 0);
                glColor4f(1f, 1f, 1f, 1f);
                glDisable(GL_BLEND);
                glEnable(GL_DEPTH_TEST);
            }

            if (!gameInputBlocked &&
                Input.isMouseJustPressed(GLFW_MOUSE_BUTTON_RIGHT) &&
                targeted != null) {

                int tx = targeted[0];
                int ty = targeted[1];
                int tz = targeted[2];

                int targetedBlock = world.getBlock(tx, ty, tz);

                if (targetedBlock == Chunk.CRAFTING_TABLE) {
                    steveRenderer.startPlace();
                    craftingTableOpen = true;
                    inventoryOpen = false;
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                    Input.resetDeltas();
                } else {
                    int px = targeted[0] + targeted[3];
                    int py = targeted[1] + targeted[4];
                    int pz = targeted[2] + targeted[5];

                    float halfW = Player.WIDTH / 2f;
                    boolean insidePlayer =
                        px + 1 > player.position.x - halfW &&
                        px < player.position.x + halfW &&
                        py + 1 > player.position.y &&
                        py < player.position.y + Player.HEIGHT &&
                        pz + 1 > player.position.z - halfW &&
                        pz < player.position.z + halfW;

                    if (!insidePlayer) {
                        int selected = hotbar.getSelectedItem();

                        if (selected != BlockTypes.AIR) {
                            int placedBlock = getPlacedBlock(selected);

                            if (placedBlock != BlockTypes.AIR) {
                                steveRenderer.startPlace();
                                world.setBlock(px, py, pz, placedBlock);
                                hotbar.removeSelectedItem();
                            }
                        }
                    }
                }
            }

            if (craftingTableOpen) {
                craftingTableScreen.render(hotbar);
            } else if (inventoryOpen) {
                inventory.render(hotbar);
            } else {
                hotbar.render();
                crosshair.render();
            }

            chatOverlay.render(hotbar, chatFont, glfwGetTime());

            Input.resetMouseJustPressed();
            Input.resetKeyJustPressed();

            glfwSwapBuffers(window);
            glfwPollEvents();

            if (!chatOverlay.isOpen() && Input.isKeyDown(GLFW_KEY_ESCAPE)) {
                glfwSetWindowShouldClose(window, true);
            }
        }
    }

    private void updateItemPickup(float deltaTime) {
        Iterator<ItemEntity> it = world.getItemEntities().iterator();

        while (it.hasNext()) {
            ItemEntity item = it.next();

            float px = player.position.x;
            float py = player.position.y + 0.9f;
            float pz = player.position.z;

            float dx = px - item.position.x;
            float dy = py - item.position.y;
            float dz = pz - item.position.z;

            float distSq = dx * dx + dy * dy + dz * dz;

            if (item.pickupDelay <= 0f && distSq < 1.0f) {
                for (int i = 0; i < item.count; i++) {
                    hotbar.addItem(item.itemId);
                }
                it.remove();
                continue;
            }

            if (item.pickupDelay <= 0f && distSq < 2.25f) {
                float dist = (float) Math.sqrt(distSq);

                if (dist > 0.001f) {
                    float nx = dx / dist;
                    float ny = dy / dist;
                    float nz = dz / dist;

                    float strength = 1.0f - (dist / 3.0f);
                    if (strength < 0f) strength = 0f;

                    float pull = (0.12f + strength * strength * 0.32f);

                    item.velocity.x += nx * pull;
                    item.velocity.y += ny * pull;
                    item.velocity.z += nz * pull;
                }
            }
        }
    }

    private void dropSelectedItem() {
        int selectedItem = hotbar.getSelectedItem();
        if (selectedItem == BlockTypes.AIR) return;

        Vector3f front = camera.getFront();
        float spawnX = player.position.x + front.x * 0.8f;
        float spawnY = player.position.y + 1.2f;
        float spawnZ = player.position.z + front.z * 0.8f;

        world.spawnItem(selectedItem, 1, spawnX, spawnY, spawnZ);
        hotbar.removeSelectedItem();

        if (!world.getItemEntities().isEmpty()) {
            ItemEntity dropped = world.getItemEntities().get(world.getItemEntities().size() - 1);
            dropped.velocity.set(front.x * 4f, 1.8f, front.z * 4f);
            dropped.pickupDelay = 0.35f;
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }

    private int getPlacedBlock(int itemId) {
        switch (itemId) {
            case ItemTypes.DIRT:
                return Chunk.DIRT;
            case ItemTypes.GRASS:
                return Chunk.GRASS;
            case ItemTypes.STONE:
                return Chunk.STONE;
            case ItemTypes.OAK_LOG:
                return Chunk.OAK_LOG;
            case ItemTypes.OAK_LEAVES:
                return Chunk.OAK_LEAVES;
            case ItemTypes.OAK_PLANKS:
                return Chunk.OAK_PLANKS;
            case ItemTypes.CRAFTING_TABLE:
                return Chunk.CRAFTING_TABLE;
            case ItemTypes.COBBLESTONE:
                return Chunk.COBBLESTONE;
            default:
                return Chunk.AIR;
        }
    }
}