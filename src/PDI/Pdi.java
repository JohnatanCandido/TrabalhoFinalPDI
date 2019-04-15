package PDI;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.util.*;

public class Pdi {

    private static int[][] matrizGaussiana = {
            {2, 7, 12, 7, 2},
            {7, 31, 52, 31, 7},
            {15, 52, 127, 52, 15},
            {7, 31, 52, 31, 7,},
            {2, 7, 12, 7, 2}
    };

    public static Image gaussian(Image img) {
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();

        WritableImage wi = new WritableImage(w, h);
        PixelReader pr = img.getPixelReader();
        PixelWriter pw = wi.getPixelWriter();

        for (int i = 2; i < w - 2; i++) {
            for (int j = 2; j < h - 2; j++) {
                double red = 0.0, green = 0.0, blue = 0.0;

                for (int ii = -2; ii < 3; ii++) {
                    for (int jj = -2; jj < 3; jj++) {
                        int iw = i + ii;
                        int jh = j + jj;

                        red += (pr.getColor(iw, jh).getRed() * matrizGaussiana[ii + 2][jj + 2]);
                        green += (pr.getColor(iw, jh).getGreen() * matrizGaussiana[ii + 2][jj + 2]);
                        blue += (pr.getColor(iw, jh).getBlue() * matrizGaussiana[ii + 2][jj + 2]);
                    }
                }
                int total = 0;
                for (int ii = 0; ii < 5; ii++) {
                    for (int jj = 0; jj < 5; jj++) {
                        total += matrizGaussiana[ii][jj];
                    }
                }

                pw.setColor(i, j, new Color(red / total, green / total, blue / total, pr.getColor(i, j).getOpacity()));
            }
        }
        return wi;
    }

    public static double[][][] converteImagemRgbParaHsb(double[][][] pixeis) {
        for (int i = 0; i < pixeis.length; i++) {
            for (int j = 0; j < pixeis[i].length; j++) {
                pixeis[i][j] = convertePixelRgbParaHsb(pixeis[i][j][0], pixeis[i][j][1], pixeis[i][j][2]);
            }
        }
        return pixeis;
    }

    public static double[][][] converteImagemHsbParaRgb(double[][][] pixeis) {
        for (int i = 0; i < pixeis.length; i++) {
            for (int j = 0; j < pixeis[i].length; j++) {
                pixeis[i][j] = convertePixelHsbParaRgb(pixeis[i][j][0], pixeis[i][j][1], pixeis[i][j][2]);
            }
        }
        return pixeis;
    }

    private static double[] convertePixelRgbParaHsb(double red, double green, double blue) {
        double min = Math.min(Math.min(red, green), blue);
        double max = Math.max(Math.max(red, green), blue);

        double h = -10, s, v;
        double delta = max - min;

        v = max;

        if (max == 0)
            s = 0;
        else {
            s = delta / max;
        }

        if (max == min)
            h = 0;
        else if (max == red && green > blue)
            h = 60 * ((green - blue) / delta);
        else if (max == red && green < blue)
            h = 60 * ((green - blue) / delta) + 360;
        else if (max == green)
            h = 60 * ((blue - red) / delta) + 120;
        else if (max == blue)
            h = 60 * ((red - green) / delta) + 240;

        return new double[]{h, s, v};
    }

    private static double[] convertePixelHsbParaRgb(double h, double s, double v) {
        Double hi = h / 60;
        int hi_int = hi.intValue();
        double f = (h / 60) - hi_int;
        double p = v * (1 - s);
        double q = v * (1 - f * s);
        double t = v * (1 - (1 - f) * s);

        if (hi_int == 0)
            return new double[]{v, t, p};
        if (hi_int == 1)
            return new double[]{q, v, p};
        if (hi_int == 2)
            return new double[]{p, v, t};
        if (hi_int == 3)
            return new double[]{p, q, v};
        if (hi_int == 4)
            return new double[]{t, p, v};
        if (hi_int == 5)
            return new double[]{v, p, q};
        throw new NumberFormatException("Deveria ter retornado o valor em RGB");
    }

    public static double[][][] segmentar(final double[][][] pixeis, int seeds, boolean isRGB) {
        List<List<double[]>> grupos = new ArrayList<>();

        // inicia centro aleatoriamente
        double[][] centros = new double[seeds][3];
        for (int i = 0; i < centros.length; i++) {
            if (isRGB)
                centros[i] = new double[]{new Random().nextDouble() * 255, new Random().nextDouble() * 255, new Random().nextDouble() * 255};
            else
                centros[i] = new double[]{new Random().nextDouble() * 359, new Random().nextDouble(), new Random().nextDouble()};
            grupos.add(new ArrayList<>());
        }

        double[][][] centrosAntigos = new double[25][seeds][3];

        int iteracoes = 0;
        do {
            iteracoes++;
            for (int a = 1; a < centrosAntigos.length; a++) {
                centrosAntigos[a - 1] = copiaCentros(centrosAntigos[a]);
            }
            centrosAntigos[centrosAntigos.length - 1] = copiaCentros(centros);

            for (List<double[]> grupo : grupos) {
                grupo.clear();
            }

            for (double[][] linha : pixeis) {
                for (double[] pixel : linha) {
                    int grupo = pegaGrupoPixel(pixel, centros);
                    grupos.get(grupo).add(pixel);
                }
            }

            centros = calculaCentroGrupos(grupos);
        } while (verificaMudancaCentros(centros, centrosAntigos));

        System.out.println("Convergiu em "+iteracoes+" iterações.");
        for (int i = 0; i < pixeis.length; i++) {
            for (int j = 0; j < pixeis[i].length; j++) {
                int grupo = pegaGrupoPixel(pixeis[i][j], centros);
                pixeis[i][j] = centros[grupo];
            }
        }
        return pixeis;
    }

    @SuppressWarnings("all")
    private static double[][] copiaCentros(double[][] centroAtual) {
        double[][] copia = new double[centroAtual.length][centroAtual[0].length];
        for (int i = 0; i < centroAtual.length; i++) {
            for (int j=0; j<centroAtual[i].length; j++) {
                copia[i][j] = centroAtual[i][j];
            }
        }
        return copia;
    }

    private static int pegaGrupoPixel(double[] cores, double[][] centros) {
        List<Double> distancias = new ArrayList<>();


        for (double[] centro : centros) {
            distancias.add(Math.sqrt(Math.pow((cores[0] - centro[0]), 2) + Math.pow((cores[1] - centro[1]), 2) + Math.pow((cores[2] - centro[2]), 2)));
        }

        int g = Integer.MAX_VALUE;
        double distanciaAntiga = Double.MAX_VALUE;
        for (double d : distancias) {
            if (d < distanciaAntiga) {
                g = distancias.indexOf(d);
                distanciaAntiga = d;
            }
        }

        return g;
    }

    @SuppressWarnings("all")
    private static boolean verificaMudancaCentros(double[][] centrosAtuais, double[][][] centrosAntigos) {
        for (double[][] ca : centrosAntigos) {
            for (int i = 0; i < centrosAtuais.length; i++) {
                for (int j = 0; j < 3; j++) {
                    if (centrosAtuais[i][j] != ca[i][j])
                        return true;
                }
            }
        }
        return false;
    }

    private static double[][] calculaCentroGrupos(List<List<double[]>> grupos) {
        double[][] centros = new double[grupos.size()][3];
        for (List<double[]> g : grupos) {
            centros[grupos.indexOf(g)] = calculaCentro(g);
        }

        return centros;
    }

    private static double[] calculaCentro(List<double[]> pixeis) {
        double[] soma = {0, 0, 0};
        for (double[] pixel : pixeis) {
            soma[0] += pixel[0];
            soma[1] += pixel[1];
            soma[2] += pixel[2];
        }
        if (pixeis.size() > 0) {
            soma[0] /= pixeis.size();
            soma[1] /= pixeis.size();
            soma[2] /= pixeis.size();
        }
        return soma;
    }
}
