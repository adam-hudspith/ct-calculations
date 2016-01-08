/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.ct.computations.calculations

import uk.gov.hmrc.ct.computations.{CP286, CP294, CP998}

trait TradingLossesThisAndLaterPeriodCalculation {

  def tradingLosses(cp286: CP286, cp998: CP998): CP294 = {
    CP294(cp286.orZero + cp998.orZero)
  }
}
