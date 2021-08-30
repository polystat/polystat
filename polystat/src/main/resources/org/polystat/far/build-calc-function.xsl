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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="build-calc-function" version="2.0">
  <!--
  This XSL is used in the Calc.java class in order to turn "rules.txt"
  into a new XSL document. Yes, this XSL produces another XSL. Run
  CalcTest.java to see the example of it.
  -->
  <xsl:strip-space elements="*"/>
  <xsl:template match="inputs">
    <xsl:variable name="inputs" select="."/>
    <xsl:element name="xsl:choose">
      <xsl:for-each select="input[1]/x">
        <xsl:variable name="pos" select="position()"/>
        <xsl:element name="xsl:when">
          <xsl:attribute name="test">
            <xsl:text>$pos = </xsl:text>
            <xsl:value-of select="$pos"/>
          </xsl:attribute>
          <xsl:for-each select="$inputs/input/x[$pos]">
            <xsl:element name="v">
              <xsl:choose>
                <xsl:when test=". = 'y'">
                  <xsl:element name="xsl:value-of">
                    <xsl:attribute name="select">
                      <xsl:text>$y</xsl:text>
                    </xsl:attribute>
                  </xsl:element>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="."/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:element>
          </xsl:for-each>
        </xsl:element>
      </xsl:for-each>
    </xsl:element>
  </xsl:template>
  <xsl:template match="rule">
    <xsl:element name="xsl:when">
      <xsl:attribute name="test">
        <xsl:text>$func = '</xsl:text>
        <xsl:value-of select="f"/>
        <xsl:text>'</xsl:text>
        <xsl:if test="y != 'y'">
          <xsl:text> and $y = '</xsl:text>
          <xsl:value-of select="y"/>
          <xsl:text>'</xsl:text>
        </xsl:if>
      </xsl:attribute>
      <xsl:apply-templates select="inputs"/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="rules">
    <xsl:element name="xsl:stylesheet" exclude-result-prefixes="#all">
      <xsl:namespace name="ps">
        <xsl:text>https://www.polystat.org</xsl:text>
      </xsl:namespace>
      <xsl:namespace name="xs">
        <xsl:text>http://www.w3.org/2001/XMLSchema</xsl:text>
      </xsl:namespace>
      <xsl:attribute name="version">
        <xsl:text>2.0</xsl:text>
      </xsl:attribute>
      <xsl:element name="xsl:function">
        <xsl:attribute name="name">
          <xsl:text>ps:calc</xsl:text>
        </xsl:attribute>
        <xsl:element name="xsl:param">
          <xsl:attribute name="name">
            <xsl:text>func</xsl:text>
          </xsl:attribute>
          <xsl:attribute name="as">
            <xsl:text>xs:string</xsl:text>
          </xsl:attribute>
        </xsl:element>
        <xsl:element name="xsl:param">
          <xsl:attribute name="name">
            <xsl:text>pos</xsl:text>
          </xsl:attribute>
          <xsl:attribute name="as">
            <xsl:text>xs:integer</xsl:text>
          </xsl:attribute>
        </xsl:element>
        <xsl:element name="xsl:param">
          <xsl:attribute name="name">
            <xsl:text>y</xsl:text>
          </xsl:attribute>
          <xsl:attribute name="as">
            <xsl:text>xs:string</xsl:text>
          </xsl:attribute>
        </xsl:element>
        <xsl:element name="xsl:choose">
          <xsl:element name="xsl:when">
            <xsl:attribute name="test">
              <xsl:text>$y = '\any'</xsl:text>
            </xsl:attribute>
            <xsl:element name="v">
              <xsl:text>\any</xsl:text>
            </xsl:element>
          </xsl:element>
          <xsl:element name="xsl:otherwise">
            <xsl:element name="xsl:choose">
              <xsl:apply-templates select="rule"/>
              <xsl:element name="xsl:otherwise">
                <xsl:element name="v">
                  <xsl:text>NONE</xsl:text>
                </xsl:element>
              </xsl:element>
            </xsl:element>
          </xsl:element>
        </xsl:element>
      </xsl:element>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
