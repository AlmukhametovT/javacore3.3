import ru.ufa.GameProgress;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {
    public static final String LINK_SAVE_GAMES = "C:\\Users\\TAU\\IdeaProjects\\javacore3.3\\Games\\savegames";
    public static final String LINK_REPORT = "C:\\Users\\TAU\\IdeaProjects\\javacore3.3\\Games\\temp\\temp.txt";
    public static final boolean CLEAR_REPORT_ON_START = false;
    public static List<String> saveGamesList = new ArrayList<>();

    public static void main(String[] args) {
        String gamesLink = "C:\\Users\\TAU\\IdeaProjects\\javacore3.3\\Games\\";
        StringBuilder installReport = new StringBuilder();

        installReport.append(createDir(gamesLink + "src"));
        installReport.append(createDir(gamesLink + "res"));
        installReport.append(createDir(gamesLink + "savegames"));
        installReport.append(createDir(gamesLink + "temp"));
        installReport.append(createDir(gamesLink + "src\\main"));
        installReport.append(createDir(gamesLink + "src\\test"));
        installReport.append(createFile(gamesLink + "src\\main\\Main.java"));
        installReport.append(createFile(gamesLink + "src\\main\\Utils.java"));
        installReport.append(createDir(gamesLink + "res\\drawables"));
        installReport.append(createDir(gamesLink + "res\\vectors"));
        installReport.append(createDir(gamesLink + "res\\icons"));
        installReport.append(createFile(gamesLink + "temp\\temp.txt"));

        addReport(gamesLink + "temp\\temp.txt", !CLEAR_REPORT_ON_START, String.valueOf(installReport));

        GameProgress gameProgress1 = new GameProgress(94, 10, 2, 254.32);
        GameProgress gameProgress2 = new GameProgress(86, 13, 4, 316.36);
        GameProgress gameProgress3 = new GameProgress(30, 50, 12, 1666.66);

        saveGame(LINK_SAVE_GAMES + "\\save1.dat", gameProgress1);
        saveGame(LINK_SAVE_GAMES + "\\save2.dat", gameProgress2);
        saveGame(LINK_SAVE_GAMES + "\\save3.dat", gameProgress3);

        zipFiles(LINK_SAVE_GAMES + "\\zip.zip");

        deleteSaveGames();

        openZip(LINK_SAVE_GAMES + "\\zip.zip", LINK_SAVE_GAMES);

        GameProgress randomGameProgress = openProgress(saveGamesList.get(new Random().nextInt(saveGamesList.size())));

        System.out.println("Состояние случайной сохранненой игры:");
        System.out.println(randomGameProgress);
    }

    public static String createDir(String dirLink) {
        File dir = new File(dirLink);
        if (dir.mkdir()) {
            return dir.getName() + " - Каталог создан" + "\n";
        } else return dir.getName() + " - Каталог создать не удалось" + "\n";
    }

    public static String createFile(String fileLink) {
        File file = new File(fileLink);
        try {
            if (file.createNewFile())
                return file.getName() + " - Файл создан" + "\n";
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return file.getName() + " - Файл создать не удалось" + "\n";
    }

    public static void addReport(String linkReport, boolean append, String reportText) {
        try (FileWriter writer = new FileWriter(linkReport, append)) {
            writer.append('\n');
            writer.append(reportText);
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void saveGame(String link, GameProgress gameProgress) {
        try (FileOutputStream fos = new FileOutputStream(link);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(gameProgress);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        saveGamesList.add(link);
        addReport(LINK_REPORT, true, "Игра сохранена. Файл сохранения - " + link);
    }

    public static void zipFiles(String zipLink) {
        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipLink))) {
            for (int i = 0; i < saveGamesList.size(); i++) {
                try (FileInputStream fis = new FileInputStream(saveGamesList.get(i))) {
                    ZipEntry entry = new ZipEntry(saveGamesList.get(i)
                            .substring(saveGamesList.get(i).lastIndexOf("\\") + 1));
                    zout.putNextEntry(entry);
                    byte[] buffer = new byte[fis.available()];
                    fis.read(buffer);
                    zout.write(buffer);
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
                zout.closeEntry();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        addReport(LINK_REPORT, true, "Архивация сохранений. Файл архива - " + zipLink);
    }

    public static void deleteSaveGames() {
        for (int i = 0; i < saveGamesList.size(); i++) {
            File file = new File(saveGamesList.get(i));
            file.delete();
        }

        saveGamesList.clear();
        addReport(LINK_REPORT, true, "Каталог с сохранениями очищен.");
    }

    public static void openZip(String zipLink, String wayForExtraction) {
        try (ZipInputStream zin = new ZipInputStream(new FileInputStream(zipLink))) {
            ZipEntry entry;
            String name;
            while ((entry = zin.getNextEntry()) != null) {
                name = entry.getName();
                FileOutputStream fout = new FileOutputStream(wayForExtraction + "\\" + name);
                saveGamesList.add(wayForExtraction + "\\" + name);
                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }
                fout.flush();
                zin.closeEntry();
                fout.close();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        addReport(LINK_REPORT, true, "Каталог с сохранениями восстановлен из архива.");
    }

    public static GameProgress openProgress(String saveFileLink) {
        GameProgress gameProgress = null;
        try (FileInputStream fis = new FileInputStream(saveFileLink);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            gameProgress = (GameProgress) ois.readObject();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        addReport(LINK_REPORT, true, "Произведена десериализация сохраненной игры - " + saveFileLink);
        return gameProgress;
    }
}