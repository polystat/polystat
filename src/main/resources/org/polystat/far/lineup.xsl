<?xml version="1.0"?>
<!--
The MIT License (MIT)

Copyright (c) 2020-2021 Polystat.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ps="https://www.polystat.org" xmlns:xs="http://www.w3.org/2001/XMLSchema" id="calculate" version="2.0">
  <xsl:strip-space elements="*"/>
  <xsl:template match="o//opts">
    <xsl:variable name="r" select="."/>
    <xsl:variable name="y" select="/o/@expected"/>
    <xsl:for-each select="ps:calc($r/@f, $r/@pos, $y)">
      <xsl:call-template name="print">
        <xsl:with-param name="f" select="$r/@f"/>
        <xsl:with-param name="y" select="$y"/>
        <xsl:with-param name="x" select="."/>
        <xsl:with-param name="pos" select="$r/@pos"/>
        <xsl:with-param name="tau">
          <xsl:text>&#x1D70F;</xsl:text>
          <xsl:value-of select="$r/@tau"/>
          <xsl:text>=</xsl:text>
          <xsl:value-of select="position()"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>
  <xsl:template match="r[opt]">
    <xsl:variable name="r" select="."/>
    <xsl:for-each select="opt">
      <xsl:variable name="opt" select="."/>
      <xsl:for-each select="ps:calc($r/@f, $r/@pos, @x)">
        <xsl:call-template name="print">
          <xsl:with-param name="f" select="$r/@f"/>
          <xsl:with-param name="y">
            <xsl:value-of select="$opt/@m"/>
            <xsl:text>=</xsl:text>
            <xsl:value-of select="$opt/@x"/>
          </xsl:with-param>
          <xsl:with-param name="x" select="."/>
          <xsl:with-param name="pos" select="$r/@pos"/>
          <xsl:with-param name="tau">
            <xsl:text>&#x1D70F;</xsl:text>
            <xsl:value-of select="$r/@tau"/>
            <xsl:text>=</xsl:text>
            <xsl:value-of select="position()"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="$opt/text()"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
