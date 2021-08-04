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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" id="expressions-to-inputs" version="2.0">
  <!--
  This XSL turns boolean expressions into <input> elements. For example,
  this XML:

  <o name="foo">
    <o line="3" name="a">
      <b x="\any">((t4=1 ∧ t3=1))</b>
    </o>
  </o>

  Will turn into:

  <o name="foo">
    <input>
      <a attr="a" x="\any">((t4=1 ∧ t3=1))</a>
      <expr>((t4=1 ∧ t3=1))</expr>
    </input>
  </o>
  -->
  <xsl:strip-space elements="*"/>
  <xsl:template name="print">
    <xsl:param name="pre" as="node()*"/>
    <xsl:param name="next" as="node()?"/>
    <xsl:choose>
      <xsl:when test="$next">
        <xsl:for-each select="$next/b">
          <xsl:call-template name="print">
            <xsl:with-param name="pre">
              <xsl:copy-of select="$pre"/>
              <a>
                <xsl:attribute name="attr">
                  <xsl:value-of select="$next/@name"/>
                </xsl:attribute>
                <xsl:attribute name="x">
                  <xsl:value-of select="@x"/>
                </xsl:attribute>
                <xsl:value-of select="text()"/>
              </a>
            </xsl:with-param>
            <xsl:with-param name="next" select="$next/following-sibling::o[b][1]"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="input">
          <xsl:for-each select="$pre">
            <xsl:copy-of select="."/>
          </xsl:for-each>
          <xsl:element name="expr">
            <xsl:for-each select="$pre/a">
              <xsl:if test="position() &gt; 1">
                <xsl:text> and </xsl:text>
              </xsl:if>
              <xsl:value-of select="text()"/>
            </xsl:for-each>
          </xsl:element>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="o[o[b]]">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
      <xsl:call-template name="print">
        <xsl:with-param name="pre" select="nothing"/>
        <xsl:with-param name="next" select="o[b][1]"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="o[b]">
    <xsl:copy>
      <xsl:apply-templates select="@*|node() except b"/>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
