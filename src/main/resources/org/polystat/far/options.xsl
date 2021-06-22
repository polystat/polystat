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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="reverses" version="2.0">
  <xsl:strip-space elements="*"/>
  <xsl:template match="calc">
    <xsl:param name="f"/>
    <xsl:param name="pos"/>
    <xsl:param name="tau"/>
    <xsl:param name="expected"/>
    <xsl:choose>
      <xsl:when test="$f = '.add'">

      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="opts">
    <xsl:param name="r" as="element"/>
    <xsl:choose>
      <xsl:when test="$r[r]">
        <xsl:variable name="kids">
          <xsl:call-template name="opts">
            <xsl:with-param name="r" select="$r/r[1]"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:for-each select="$kids">
          <xsl:call-template name="calc">
            <xsl:with-param name="r" select="$r/r[1]"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:when>
    </xsl:choose>
    <xsl:ele>
      <xsl:apply-templates select="@*"/>
    </xsl:ele>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
