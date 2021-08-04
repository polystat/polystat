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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="opts-to-expressions" version="2.0">
  <!--
  This XSL will fold all <opts> elements into more compact
  textual boolean expressions. For example, this XML:

  <o name="foo">
    <o line="3" name="a">
      <opts>
        <opt m="xxx" x="\any">
          <tau i="4:1">1</tau>
          <tau i="3:2">1</tau>
        </opt>
      </opts>
    </o>
  </o>

  Will turn into:

  <o name="foo">
    <o line="3" name="a">
      <b x="\any">((t4=1 ∧ t3=1))</b>
    </o>
  </o>
  -->
  <xsl:strip-space elements="*"/>
  <xsl:template match="o[opts]">
    <xsl:variable name="o" select="."/>
    <xsl:copy>
      <xsl:apply-templates select="@*|node() except opts"/>
      <xsl:for-each select="distinct-values(opts/opt/@x)">
        <xsl:variable name="x" select="."/>
        <xsl:element name="b">
          <xsl:attribute name="x">
            <xsl:value-of select="$x"/>
          </xsl:attribute>
          <xsl:for-each select="$o/opts[opt[@x=$x or @x='\any']]">
            <xsl:if test="position() &gt; 1">
              <xsl:text> and </xsl:text>
            </xsl:if>
            <xsl:text>(</xsl:text>
            <xsl:for-each select="opt[@x=$x or @x='\any' and tau]">
              <xsl:if test="position() &gt; 1">
                <xsl:text> or </xsl:text>
              </xsl:if>
              <xsl:text>(</xsl:text>
              <xsl:for-each select="tau">
                <xsl:if test="position() &gt; 1">
                  <xsl:text> &#x2227; </xsl:text>
                </xsl:if>
                <xsl:text>&#x1D70F;</xsl:text>
                <xsl:variable name="parts" select="tokenize(@i, ':')"/>
                <xsl:value-of select="$parts[1]"/>
                <xsl:text>=</xsl:text>
                <xsl:value-of select="text()"/>
              </xsl:for-each>
              <xsl:text>)</xsl:text>
            </xsl:for-each>
            <xsl:text>)</xsl:text>
          </xsl:for-each>
        </xsl:element>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
