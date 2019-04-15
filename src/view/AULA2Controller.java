package view;

import PDI.Pdi;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@SuppressWarnings("unused")
public class AULA2Controller {

    private static final String SEARCH_DIRECTORY = "C:\\Users\\Administrador\\Documents\\PDI\\segmentacao";
    private static final String SAVE_DIRECTORY = "C:\\Users\\Administrador\\Documents\\PDI\\resultados";

    @FXML
    ImageView img1, img2, img3;

    @FXML
    TextField qtSeeds, iteracoes;

    private Image imagem1, imagem2, imagem3;

    private File f;

    @FXML
    @SuppressWarnings("unused")
    public void abreImagem1() {
        f = selecionarImagem();

        if (f != null) {
            imagem1 = new Image(f.toURI().toString());
            img1.setImage(imagem1);
            img1.setFitWidth(imagem1.getWidth());
            img1.setFitHeight(imagem1.getHeight());
        }
    }

    private File selecionarImagem() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Imagens", "*.jpg", "*.JPG", "*.png", "*.PNG", "*.gif", "*.GIF", "*.bmp", "*.BMP"));
        fileChooser.setInitialDirectory(new File(SEARCH_DIRECTORY));

        try {
            return fileChooser.showOpenDialog(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    public void salvar() {
        if (imagem2 != null && imagem3 != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.PNG"));
            fileChooser.setInitialDirectory(new File(SAVE_DIRECTORY));

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                File img1 = new File(file.getAbsolutePath().replace(".png", "RGB.png"));
                File img2 = new File(file.getAbsolutePath().replace(".png", "HSV.png"));
                BufferedImage bImg1 = SwingFXUtils.fromFXImage(imagem2, null);
                BufferedImage bImg2 = SwingFXUtils.fromFXImage(imagem3, null);
                try {
                    ImageIO.write(bImg1, "PNG", img1); //salva imagem RGB
                    ImageIO.write(bImg2, "PNG", img2); //salva imagem HSV
                    exibeMsg("Salvar imagem", "Imagens salvas com sucesso!", Alert.AlertType.CONFIRMATION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            exibeMsg("Ué", "???", Alert.AlertType.ERROR);
        }
    }

    private void exibeMsg(String titulo, String cabecalho, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.showAndWait();
    }

    private void atualizar() {
        img2.setImage(imagem2);
        img2.setFitWidth(imagem2.getWidth());
        img2.setFitHeight(imagem2.getHeight());

        img3.setImage(imagem3);
        img3.setFitWidth(imagem3.getWidth());
        img3.setFitHeight(imagem3.getHeight());
    }

    @FXML
    public void comDifusaoAnisotropica() {
        System.out.println("\n\n\n\n\nProcessando...");
        imagem2 = null;
        imagem3 = null;
        Image img = new Image(f.toURI().toString());
        for (int i = 0; i < Integer.valueOf(iteracoes.getText()); i++) {
            img = Pdi.gaussian(img);
        }
        System.out.println("Terminou filtragem anisotrópica");
        segmentar(img);
    }

    @FXML
    public void semDifusaoAnisotropica() {
        System.out.println("\n\n\n\n\nProcessando...");
        imagem2 = null;
        imagem3 = null;
        segmentar(imagem1);
    }

    private void segmentar(Image img) {
        int seeds = Integer.parseInt(qtSeeds.getText());
        //RGB
        double[][][] coresSegmentadasImagem2 = Pdi.segmentar(criaListaPixeis(img), seeds, true);
        imagem2 = criaImagem(coresSegmentadasImagem2);
        System.out.println("Terminou segmentação RGB");

        //HSV
        double[][][] coresSegmentadasImagem3 = Pdi.converteImagemRgbParaHsb(criaListaPixeis(img));
        coresSegmentadasImagem3 = Pdi.segmentar(coresSegmentadasImagem3, seeds, false);
        coresSegmentadasImagem3 = Pdi.converteImagemHsbParaRgb(coresSegmentadasImagem3);

        imagem3 = criaImagem(coresSegmentadasImagem3);
        atualizar();
        System.out.println("Done!");
    }

    private double[][][] criaListaPixeis(Image img) {
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        PixelReader pr = img.getPixelReader();

        double[][][] listaPixeis = new double[w][h][3];

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                double red = pr.getColor(i, j).getRed() * 255;
                double green = pr.getColor(i, j).getGreen() * 255;
                double blue = pr.getColor(i, j).getBlue() * 255;

                listaPixeis[i][j][0] = red;
                listaPixeis[i][j][1] = green;
                listaPixeis[i][j][2] = blue;
            }
        }
        return listaPixeis;
    }

    private Image criaImagem(double[][][] listaPixeis) {
        int w = listaPixeis.length;
        int h = listaPixeis[0].length;

        WritableImage wi = new WritableImage(w, h);
        PixelWriter pw = wi.getPixelWriter();

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                double r = listaPixeis[i][j][0] / 255;
                double g = listaPixeis[i][j][1] / 255;
                double b = listaPixeis[i][j][2] / 255;
                pw.setColor(i, j, new Color(r, g, b, 1));
            }
        }
        return wi;
    }
}
