package tool

/**
 * Created by Administrator on 2019/12/20
 */
sealed trait DomainValidation {
  def errorMessage: String
}


