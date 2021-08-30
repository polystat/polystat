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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="taus-to-tree" version="2.0">
  <!--
  This XSL simply converts textual representation of options into
  XML-tree format. For example, this XML:

  <o name="foo">
    <o line="3" name="a">
      <opts>
         <opt m="xx" x="\any">{t4:1=1} {t3:2=1}</opt>
      </opts>
    </o>
  </o>

  Will turn into:

  <o name="foo">
    <o line="3" name="a">
      <opts>
         <opt m="xx" x="\any">
            <tau i="4:1">1</tau>
            <tau i="3:2">1</tau>
         </opt>
      </opts>
    </o>
  </o>
  -->
  <xsl:strip-space elements="*"/>
  <xsl:template match="opt[not(empty(text()))]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:for-each select="tokenize(text(), ' ')">
        <xsl:variable name="parts" select="tokenize(., '=')"/>
        <xsl:element name="tau">
          <xsl:attribute name="i">
            <xsl:value-of select="substring($parts[1], 3)"/>
          </xsl:attribute>
          <xsl:value-of select="substring($parts[2], 1, string-length($parts[2]) - 1)"/>
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
