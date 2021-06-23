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
  <xsl:function name="ps:tau">
    <xsl:param name="idx" as="xs:integer"/>
    <xsl:param name="v" as="xs:integer"/>
    <xsl:text>{</xsl:text>
    <xsl:text>&#x1D70F;</xsl:text>
    <xsl:value-of select="$idx"/>
    <xsl:text>=</xsl:text>
    <xsl:value-of select="$v"/>
    <xsl:text>}</xsl:text>
  </xsl:function>
  <xsl:function name="ps:calc">
    <xsl:param name="func" as="xs:string"/>
    <xsl:param name="pos" as="xs:integer"/>
    <xsl:param name="y" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="$y = 'ANY'">
        <v>ANY</v>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="$func = '.add'">
            <xsl:choose>
              <xsl:when test="$pos = 1">
                <xsl:choose>
                  <xsl:when test="$y = '\perp'">
                    <v>\perp</v>
                    <v>ANY</v>
                  </xsl:when>
                  <xsl:otherwise>
                    <v>0</v>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:when test="$pos = 2">
                <xsl:choose>
                  <xsl:when test="$y = '\perp'">
                    <v>ANY</v>
                    <v>\perp</v>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$y"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise>
                <xsl:message terminate="yes">
                  <xsl:text>The position '</xsl:text>
                  <xsl:value-of select="$pos"/>
                  <xsl:text>' is not valid for ADD</xsl:text>
                </xsl:message>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="$func = '.div'">
            <xsl:choose>
              <xsl:when test="$pos = 1">
                <xsl:choose>
                  <xsl:when test="$y = '\perp'">
                    <v>ANY</v>
                    <v>\perp</v>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$y"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:when test="$pos = 2">
                <xsl:choose>
                  <xsl:when test="$y = '\perp'">
                    <v>0</v>
                    <v>ANY</v>
                  </xsl:when>
                  <xsl:otherwise>
                    <v>1</v>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:when>
              <xsl:otherwise>
                <xsl:message terminate="yes">
                  <xsl:text>The position '</xsl:text>
                  <xsl:value-of select="$pos"/>
                  <xsl:text>' is not valid for DIV</xsl:text>
                </xsl:message>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <v>NONE</v>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  <xsl:template name="print">
    <xsl:param name="f"/>
    <xsl:param name="x"/>
    <xsl:param name="pos"/>
    <xsl:param name="y"/>
    <xsl:param name="tau"/>
    <xsl:element name="opt">
      <xsl:attribute name="m">
        <xsl:value-of select="$f"/>
        <xsl:text>(</xsl:text>
        <xsl:value-of select="$y"/>
        <xsl:text>)[</xsl:text>
        <xsl:value-of select="$pos"/>
        <xsl:text>]</xsl:text>
      </xsl:attribute>
      <xsl:attribute name="x">
        <xsl:value-of select="$x"/>
      </xsl:attribute>
      <xsl:value-of select="$tau"/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="r[not(*)]">
    <xsl:variable name="r" select="."/>
    <xsl:variable name="y" select="/o/@expected"/>
    <xsl:for-each select="ps:calc($r/@f, $r/@pos, $y)">
      <xsl:call-template name="print">
        <xsl:with-param name="f" select="$r/@f"/>
        <xsl:with-param name="y" select="$y"/>
        <xsl:with-param name="x" select="."/>
        <xsl:with-param name="pos" select="$r/@pos"/>
        <xsl:with-param name="tau" select="ps:tau($r/@tau, position())"/>
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
            <xsl:value-of select="ps:tau($r/@tau, position())"/>
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
