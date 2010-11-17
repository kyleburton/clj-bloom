package com.github.kyleburton;

public class JBit {
  private long width = 0;
  private byte[][] bitArrays = null;

  public JBit ( long width ) {
    long basize1 = width / 8L;
    width = width % 8L;
    int basize2 = (int)(width / Integer.MAX_VALUE);

    System.out.println(String.format("width=%s; basize1=%s; basize2=%s",
          ""+width,
          ""+basize1,
          ""+basize2));

    bitArrays = new byte[basize2][];
  }

  public void set ( long bitNum ) {
    long baidx1 = bitNum / Integer.MAX_VALUE;
    bitNum = bitNum % Integer.MAX_VALUE;
    long baidx2 = bitNum / 8;
    long baoff  = bitNum % 8;
    bitArrays[(int)baidx1][(int)baidx2] &= 1>>>(int)baoff;
  }
}
