package PDI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import java.util.Random;

public class ConversorPixelTest {

    @Test
    public void conversaoTest() {
        int w = 20, h = 20;
        double[][][] pixeisAlterados = criaPixeis(w, h);
        double[][][] pixeisOriginais = duplicaImg(pixeisAlterados);

        pixeisAlterados = Pdi.converteImagemRgbParaHsb(pixeisAlterados);

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                for (int k = 0; k < 2; k++) {
                    String ori = String.valueOf(pixeisOriginais[i][j][k]).substring(0, 8);
                    String alt = String.valueOf(pixeisAlterados[i][j][k]).substring(0, 8);
                    assertNotEquals(ori, alt);
                }
            }
        }

        pixeisAlterados = Pdi.converteImagemHsbParaRgb(pixeisAlterados);

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                for (int k = 0; k < 3; k++) {
                    assertEquals(Math.round(pixeisAlterados[i][j][k]), Math.round(pixeisOriginais[i][j][k]));
                }
            }
        }

    }

    private double[][][] criaPixeis(int w, int h) {
        double[][][] pixeis = new double[w][h][3];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                pixeis[i][j] = new double[]{new Random().nextDouble() * 255,
                        new Random().nextDouble() * 255,
                        new Random().nextDouble() * 255};
            }
        }
        return pixeis;
    }

    @SuppressWarnings("all")
    private double[][][] duplicaImg(double[][][] pixeis) {
        double[][][] pixeisOriginais = new double[pixeis.length][pixeis[0].length][3];
        for (int i = 0; i < pixeis.length; i++) {
            for (int j = 0; j < pixeis[0].length; j++) {
                pixeisOriginais[i][j] = pixeis[i][j];
            }
        }
        return pixeisOriginais;
    }
}
