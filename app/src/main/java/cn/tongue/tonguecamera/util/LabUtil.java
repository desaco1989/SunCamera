package cn.tongue.tonguecamera.util;

import cn.tongue.tonguecamera.bean.Lab;

/**
 * @packageName: cn.tongue.tonguecamera.util
 * @fileName: LabUtil
 * @date: 2019/4/3  13:38
 * @author: ymc
 * @QQ:745612618
 */

public class LabUtil {


    /**
     * D50 光源下 RGB 转为lab
     *
     * @param sR r
     * @param sG g
     * @param sB b
     * @return lab
     */
    public static Lab rgbToLabD50(double sR, double sG, double sB) {
        Lab lab = new Lab();
        //double R = ( (sR+0.055)/1.055 )^2.4;
        double r = Math.pow((sR + 0.055) / 1.055, 2.4);
        //double G = ((sG+0.055)/1.055)^2.4;
        double g = Math.pow((sG + 0.055) / 1.055, 2.4);
        //double B = ((sB+0.055)/1.055)^2.4;
        double b = Math.pow((sB + 0.055) / 1.055, 2.4);

        double x = 43.60747 * r + 38.50649 * g + 14.30804 * b;
        double y = 22.25045 * r + 71.68786 * g + 6.06169 * b;
        double z = 1.39322 * r + 9.71045 * g + 71.41733 * b;

        double fx = Math.pow((x / 96.42), 0.33333);
        double fy = Math.pow((y / 100), 0.33333);
        double fz = Math.pow((z / 82.51), 0.33333);
        lab.L = 116.0f * fy - 16;
        lab.a = 500.0f * (fx - fy);
        lab.b = 200.0f * (fy - fz);
        return lab;
        //*L1 = 116.0f * FY - 16;
        //*a = 500.f * (FX - FY);
        //*b = 200.f * (FY - FZ);
    }

    public static double[] labToRgbD50(Lab lab) {
        double[] rgb = new double[3];
        double fx = lab.a / 500.0 + lab.L / 116.0 + 16.0 / 116.0;
        double fy = lab.L / 116.0 + 16.0 / 116.0;
        double fz = lab.L / 116.0 + 16.0 / 116.0 - lab.b / 200.0;

        double x = Math.pow(fx, 3) * 96.42;
        double y = Math.pow(fy, 3) * 100.0;
        double z = Math.pow(fz, 3) * 82.51;

        double r = (0.031338563677916525435413314041998 * x - 0.016168667702911806916570043637203 * y - 0.0049061477279830157153618389684598 * z);
        double g = (0.019161415526243252949341250816428 * y - 0.009787685631270271312438617629036 * x + 0.00033454116302537262951770410856161 * z);
        double b = (0.00071945168281734332048405651420669 * x - 0.0022899127729888248202248584828693 * y + 0.014052426741535712286367722955707 * z);
        rgb[0] = Math.pow(r, 1.0 / 2.4) * 1.055 - 0.055;
        rgb[1] = Math.pow(g, 1.0 / 2.4) * 1.055 - 0.055;
        rgb[2] = Math.pow(b, 1.0 / 2.4) * 1.055 - 0.055;

        return rgb;
    }


    public static double[] Lab2XYZ(double[] Lab) {
        double[] XYZ = new double[3];
        double L, a, b;
        double fx, fy, fz;
        double Xn, Yn, Zn;
//        Xn = 95.04;
//        Yn = 100;
//        Zn = 108.89;
        Xn = 96.4296;
        Yn = 100;
        Zn = 82.5106;

        L = Lab[0];
        a = Lab[1];
        b = Lab[2];

        fy = (L + 16) / 116;
        fx = a / 500 + fy;
        fz = fy - b / 200;

        if (fx > 0.2069) {
            XYZ[0] = Xn * Math.pow(fx, 3);
        } else {
            XYZ[0] = Xn * (fx - 0.1379) * 0.1284;
        }

        if ((fy > 0.2069) || (L > 8)) {
            XYZ[1] = Yn * Math.pow(fy, 3);
        } else {
            XYZ[1] = Yn * (fy - 0.1379) * 0.1284;
        }

        if (fz > 0.2069) {
            XYZ[2] = Zn * Math.pow(fz, 3);
        } else {
            XYZ[2] = Zn * (fz - 0.1379) * 0.1284;
        }

        return XYZ;
    }


    public static double[] XYZ2Lab(double[] XYZ) {
        double[] Lab = new double[3];
        double X, Y, Z;
        X = XYZ[0];
        Y = XYZ[1];
        Z = XYZ[2];
        double Xn, Yn, Zn;
//        Xn = 95.04;
//        Yn = 100;
//        Zn = 108.89;

        Xn = 96.4296;
        Yn = 100;
        Zn = 82.5106;
        double XXn, YYn, ZZn;
        XXn = X / Xn;
        YYn = Y / Yn;
        ZZn = Z / Zn;

        double fx, fy, fz;

        if (XXn > 0.008856) {
            fx = Math.pow(XXn, 0.333333);
        } else {
            fx = 7.787 * XXn + 0.137931;
        }

        if (YYn > 0.008856) {
            fy = Math.pow(YYn, 0.333333);
        } else {
            fy = 7.787 * YYn + 0.137931;
        }

        if (ZZn > 0.008856) {
            fz = Math.pow(ZZn, 0.333333);
        } else {
            fz = 7.787 * ZZn + 0.137931;
        }
        Lab[0] = 116 * fy - 16;
        Lab[1] = 500 * (fx - fy);
        Lab[2] = 200 * (fy - fz);
        return Lab;
    }


    public static double[] sRGB2XYZ(double[] sRGB) {
        double[] XYZ = new double[3];
        double sR, sG, sB;
        sR = sRGB[0];
        sG = sRGB[1];
        sB = sRGB[2];
        sR /= 255;
        sG /= 255;
        sB /= 255;

        if (sR <= 0.04045) {
            sR = sR / 12.92;
        } else {
            sR = Math.pow(((sR + 0.055) / 1.055), 2.4);
        }

        if (sG <= 0.04045) {
            sG = sG / 12.92;
        } else {
            sG = Math.pow(((sG + 0.055) / 1.055), 2.4);
        }

        if (sB <= 0.04045) {
            sB = sB / 12.92;
        } else {
            sB = Math.pow(((sB + 0.055) / 1.055), 2.4);
        }

        XYZ[0] = 43.6052025 * sR + 38.5081593 * sG + 14.3087414 * sB;
        XYZ[1] = 22.2491598 * sR + 71.6886060 * sG + 6.0621486 * sB;
        XYZ[2] = 1.3929122 * sR + 9.7097002 * sG + 71.4185470 * sB;

        return XYZ;
    }


    public static int[] XYZ2sRGB(double[] XYZ) {
        int[] sRGB = new int[3];
        double X, Y, Z;
        double dr = 0,dg = 0,db= 0;
        X = XYZ[0];
        Y = XYZ[1];
        Z = XYZ[2];

        // TODO: 2019/4/3 D65格式暂时没有找到 D50格式
//        dr = 0.032406 * X - 0.015371 * Y - 0.0049895 * Z;
//        dg = -0.0096891 * X + 0.018757 * Y + 0.00041914 * Z;
//        db = 0.00055708 * X - 0.0020401 * Y + 0.01057 * Z;

        if (dr <= 0.00313) {
            dr = dr * 12.92;
        } else {
            dr = Math.exp(Math.log(dr) / 2.4) * 1.055 - 0.055;
        }

        if (dg <= 0.00313) {
            dg = dg * 12.92;
        } else {
            dg = Math.exp(Math.log(dg) / 2.4) * 1.055 - 0.055;
        }

        if (db <= 0.00313) {
            db = db * 12.92;
        } else {
            db = Math.exp(Math.log(db) / 2.4) * 1.055 - 0.055;
        }

        dr = dr * 255;
        dg = dg * 255;
        db = db * 255;

        dr = Math.min(255, dr);
        dg = Math.min(255, dg);
        db = Math.min(255, db);

        sRGB[0] = (int) (dr + 0.5);
        sRGB[1] = (int) (dg + 0.5);
        sRGB[2] = (int) (db + 0.5);

        return sRGB;
    }

}
