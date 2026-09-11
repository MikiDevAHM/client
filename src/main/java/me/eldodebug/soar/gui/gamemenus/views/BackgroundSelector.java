package me.eldodebug.soar.gui.gamemenus.views;

import com.glideclient.Glide;
import me.eldodebug.soar.gui.gamemenus.GlideScreen;
import me.eldodebug.soar.gui.gamemenus.MenuManager;
import me.eldodebug.soar.gui.gamemenus.ViewMenuButton;
import me.eldodebug.soar.gui.gamemenus.elements.ElementBackgroundCard;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icons;
import me.eldodebug.soar.management.profile.mainmenu.BackgroundManager;
import me.eldodebug.soar.management.profile.mainmenu.impl.Background;
import me.eldodebug.soar.management.profile.mainmenu.impl.CustomBackground;
import me.eldodebug.soar.management.profile.mainmenu.impl.DefaultBackground;
import me.eldodebug.soar.management.profile.mainmenu.impl.PanoramaBackground;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.utils.file.FileUtils;
import me.eldodebug.soar.utils.mouse.Scroll;
import me.eldodebug.soar.utils.render.GridUtils;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl3.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import java.nio.ByteBuffer;
import java.io.File;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class BackgroundSelector extends GlideScreen {

	private Scroll scroll = new Scroll();

    BackgroundManager bm;
    Glide instance;
    FileManager fm;
    ScaledResolution sr;

    int cardWidthDefault = 120;
    int cardheightdefault = 65;

    int cardWidth = 120;
    int cardHeight = 65;

    int startX = 20, startY = 45;
    int padding = 15;

    private int columns = 5;


    public ArrayList<ElementBackgroundCard> cardDefault = new ArrayList<>();
    public ArrayList<ElementBackgroundCard> cardCustom = new ArrayList<>();
    public ArrayList<ElementBackgroundCard> cardPanorama = new ArrayList<>();

	public BackgroundSelector(MenuManager parent) {
		super(parent, TranslateText.SELECT_BACKGROUND.getText());

        addMenuAction(new ViewMenuButton(Icons.POWER_24, Color.RED, () -> System.exit(0)));
        addMenuAction(new ViewMenuButton(Icons.ARROW_EXIT_20, Color.ORANGE, () -> setCurrentView(getViewByClass(null))));


        instance = Glide.getInstance();
        bm = instance.getProfileManager().getBackgroundManager();
        fm = instance.getFileManager();
        for(Background bg : bm.getBackgrounds()) {
            if (bg instanceof DefaultBackground){
                if (bg.getId() == 999){
                    cardCustom.add(
                            new ElementBackgroundCard(
                                    bg, bm,
                                    0, 0,
                                    cardWidthDefault, cardheightdefault,
                                    Icons.IMAGE_ADD_20,
                                    () -> handleCustomClick(fm, bm)
                            )
                    );
                } else {
                    cardDefault.add(
                            new ElementBackgroundCard(
                                    bg, bm,
                                    0, 0,
                                    cardWidthDefault, cardheightdefault,
                                    () -> bm.setCurrentBackground(bg)
                            )
                    );
                }
            } else if (bg instanceof PanoramaBackground){
                cardPanorama.add(
                        new ElementBackgroundCard(
                                bg, bm,
                                0, 0,
                                cardWidthDefault, cardheightdefault,
                                () -> bm.setCurrentBackground(bg)
                        )
                );
            } else if (bg instanceof CustomBackground){
                cardCustom.add(
                        new ElementBackgroundCard(
                                bg, bm,
                                0, 0,
                                cardWidthDefault, cardheightdefault,
                                () -> bm.setCurrentBackground(bg),
                                Icons.DELETE_20,
                                () -> deleteCustomBackground(bg)
                        )
                );
            }
        }
	}


	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		Glide instance = Glide.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		nvg.setupAndDraw(() -> drawNanoVG(mouseX, mouseY, sr, instance, nvg)); // nanovg frame
	}

	private void drawNanoVG(int mouseX, int mouseY, ScaledResolution sr, Glide instance, NanoVGManager nvg) {
        scroll.onScroll();
        scroll.onAnimation();

        cardWidth = (sr.getScaledWidth() - ((padding * (columns - 1)) + (startX * 2)))/columns;
        cardHeight = (cardWidth * cardheightdefault) / cardWidthDefault;

        float offY = startY + scroll.getValue();
        int index;


        nvg.drawTextWithShadow("Wallpaper", startX, offY, 0xFFFFFFFF, 5, 14, Fonts.MEDIUM);
        offY += 20;
        index = 0;
        for (ElementBackgroundCard ec: cardDefault){
            ec.setPosition(startX + GridUtils.getGridX(index, columns, cardWidth, padding), offY + GridUtils.getGridY(index, columns, cardHeight, padding));
            ec.setSize(cardWidth, cardHeight);
            ec.render(nvg, mouseX, mouseY);
            index++;
        }
        offY += GridUtils.getGridHeight(cardDefault.size(), columns, cardHeight, padding) + padding;

        nvg.drawTextWithShadow("Panorama", startX, offY, 0xFFFFFFFF, 5, 14, Fonts.MEDIUM);
        offY += 20;
        index = 0;
        for (ElementBackgroundCard ec: cardPanorama){
            ec.setPosition(startX + GridUtils.getGridX(index, columns, cardWidth, padding), offY + GridUtils.getGridY(index, columns, cardHeight, padding));
            ec.setSize(cardWidth, cardHeight);
            ec.render(nvg, mouseX, mouseY);
            index++;
        }
        offY += GridUtils.getGridHeight(cardPanorama.size(), columns, cardHeight, padding) + padding;

        nvg.drawTextWithShadow("Custom", startX, offY, 0xFFFFFFFF, 5, 14, Fonts.MEDIUM);
        offY += 20;
        index = 0;
        for (ElementBackgroundCard ec: cardCustom){
            ec.setPosition(startX + GridUtils.getGridX(index, columns, cardWidth, padding), offY + GridUtils.getGridY(index, columns, cardHeight, padding));
            ec.setSize(cardWidth, cardHeight);
            ec.render(nvg, mouseX, mouseY);
            index++;
        }

        offY += GridUtils.getGridHeight(cardCustom.size(), columns, cardHeight, padding);

        scroll.setMaxScroll(Math.max(0, ((offY - scroll.getValue()) + startY) - sr.getScaledHeight()));
	}

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        new ArrayList<>(cardDefault).forEach(ec -> ec.mouseClicked(mouseX, mouseY, mouseButton));
        new ArrayList<>(cardPanorama).forEach(ec -> ec.mouseClicked(mouseX, mouseY, mouseButton));
        new ArrayList<>(cardCustom).forEach(ec -> ec.mouseClicked(mouseX, mouseY, mouseButton));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        new ArrayList<>(cardDefault).forEach(ec -> ec.mouseReleased(mouseX, mouseY, mouseButton));
        new ArrayList<>(cardPanorama).forEach(ec -> ec.mouseReleased(mouseX, mouseY, mouseButton));
        new ArrayList<>(cardCustom).forEach(ec -> ec.mouseReleased(mouseX, mouseY, mouseButton));
    }

    public void handleCustomClick(FileManager fm, BackgroundManager bm) {
        Multithreading.runAsync(() -> {
            File file = selectImageFile();

            if (file == null || !file.exists()) return;

            File bgCacheDir = new File(fm.getCacheDir(), "background");
            if (!bgCacheDir.exists()) bgCacheDir.mkdirs();

            String ext = FileUtils.getExtension(file).toLowerCase();

            if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg")) {
                File destFile = new File(bgCacheDir, file.getName());

                try {
                    FileUtils.copyFile(file, destFile);
                    bm.addCustomBackground(destFile);

                    CustomBackground addedBG = null;
                    for (Background backs : bm.getBackgrounds()) {
                        if (backs instanceof CustomBackground) {
                            if (((CustomBackground) backs).getImage().equals(destFile)) {
                                addedBG = (CustomBackground) backs;
                            }
                        }
                    }

                    if (addedBG == null) return;
                    CustomBackground theAddedBG = addedBG;

                    ElementBackgroundCard newCard = new ElementBackgroundCard(
                            addedBG, bm,
                            0, 0,
                            cardWidthDefault, cardheightdefault,
                            () -> bm.setCurrentBackground(theAddedBG),
                            Icons.DELETE_20,
                            () -> deleteCustomBackground(theAddedBG)
                    );

                    if (!cardCustom.isEmpty()) {
                        cardCustom.add(cardCustom.size() - 1, newCard);
                    } else {
                        cardCustom.add(newCard);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void deleteCustomBackground(Background bg){
        if(bm.getCurrentBackground().equals(bg)) {
            bm.setCurrentBackground(bm.getBackgroundById(0));
        }

        cardCustom.removeIf(ec -> ec.getBg() == bg);

        bm.removeCustomBackground((CustomBackground) bg);
    }

    @Override
    public void initGui() {
        sr = new ScaledResolution(mc);
        columns = Math.min(Math.max(GridUtils.getPossibleColumns(sr.getScaledWidth(), cardWidthDefault, padding), 1), 5);
        super.initGui();
    }

    /*
     * if given the option to do this all again
     * i think id just kill myself instead
     * imagine having to manually patch tinyfd
     * and then face your own stupidity of having a folder inside the zip instead of the stuff at root :)
     * i guess this is a criticism of mac os too
     */
    public static File selectImageFile() {
        String path = null;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(3);
            filters.put(stack.UTF8("*.png"));
            filters.put(stack.UTF8("*.jpg"));
            filters.put(stack.UTF8("*.jpeg"));
            filters.flip();

            ByteBuffer title = stack.UTF8("Select Wallpaper Image");
            ByteBuffer defaultPath = stack.UTF8("");
            ByteBuffer description = stack.UTF8("Image Files (*.png, *.jpg, *.jpeg)");

            path = TinyFileDialogs.tinyfd_openFileDialog(
                    title,
                    defaultPath,
                    filters,
                    description,
                    false
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return path != null ? new File(path) : null;
    }
}
