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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ps="https://www.polystat.org" id="cleanup-conflicts" version="2.0">
  <xsl:strip-space elements="*"/>
  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
  <xsl:function name="ps:taus">
    <xsl:param name="left" as="node()"/>
    <xsl:param name="right" as="node()"/>
    <xsl:for-each select="$left/tau">
      <xsl:if test="$right/tau[@i=current()/@i and text()!=current()/text()]">
        <xsl:copy-of select="."/>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="$right/tau">
      <xsl:if test="$left/tau[@i=current()/@i and text()!=current()/text()]">
        <xsl:copy-of select="."/>
      </xsl:if>
    </xsl:for-each>
  </xsl:function>
  <xsl:function name="ps:conflicts">
    <xsl:param name="opt" as="node()"/>
    <xsl:param name="list"/>
    <xsl:for-each select="$list">
      <xsl:variable name="t" select="ps:taus($opt, .)"/>
      <xsl:if test="count($t) &gt; 0">
        <xsl:element name="c">
          <xsl:value-of select="position()"/>
          <xsl:text>/</xsl:text>
          <xsl:value-of select="count($t)"/>
          <xsl:text>:</xsl:text>
          <xsl:value-of select="./@x"/>
        </xsl:element>
      </xsl:if>
    </xsl:for-each>
  </xsl:function>
  <xsl:template match="opt">
    <xsl:variable name="o" select="."/>
    <xsl:variable name="conflicts">
      <xsl:for-each select="parent::opts/parent::o/opts[generate-id() != generate-id(current()/parent::opts)]">
        <xsl:variable name="opts" select="./opt[$o/@x=@x or @x='\any' or $o/@x='\any']"/>
        <xsl:variable name="cs" select="ps:conflicts($o, $opts)"/>
        <xsl:if test="count($cs) = count($opts)">
          <xsl:element name="v">
            <xsl:text>(</xsl:text>
            <xsl:value-of select="$cs"/>
            <xsl:text>)</xsl:text>
          </xsl:element>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="count($conflicts/*) &gt; 0">
        <xsl:comment>
          <xsl:value-of select="count($conflicts/*)"/>
          <xsl:text> conflict with OPTS: </xsl:text>
          <xsl:value-of select="$conflicts"/>
        </xsl:comment>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
