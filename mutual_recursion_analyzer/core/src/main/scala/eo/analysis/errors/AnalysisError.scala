package eo.analysis.errors

/** Analysis errors definitions. Analysis errors are those,
  * discovered during static analysis of an EO programs. They indicate either a
  * potential bug or an error that will prevent program from compiling or
  * running without errors.
  */
sealed trait AnalysisError {
  def message: String
}

case class UnresolvedNameError(name: String) extends AnalysisError {
  // TODO: add location of the usage
  override def message: String =
    s"\"${name}\" is used, but no definition for it is present."
}

