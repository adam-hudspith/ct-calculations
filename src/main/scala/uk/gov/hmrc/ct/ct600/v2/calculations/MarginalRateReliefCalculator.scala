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

package uk.gov.hmrc.ct.ct600.v2.calculations

import uk.gov.hmrc.ct.CATO04
import uk.gov.hmrc.ct.box.{CtInteger, CtTypeConverters}
import uk.gov.hmrc.ct.computations.HmrcAccountingPeriod
import uk.gov.hmrc.ct.ct600.NumberRounding
import uk.gov.hmrc.ct.ct600.calculations.{AccountingPeriodHelper, CtConstants, TaxYear, Ct600AnnualConstants}
import uk.gov.hmrc.ct.ct600.v2._
import AccountingPeriodHelper._
import Ct600AnnualConstants._


trait MarginalRateReliefCalculator extends CtTypeConverters with NumberRounding {

  def computeMarginalRateRelief(b37: B37, b44: B44, b54: B54, b38: B38, b39: B39, accountingPeriod: HmrcAccountingPeriod): CATO04 = {
    validateAccountingPeriod(accountingPeriod)

    val fy1: Int = fallsInFinancialYear(accountingPeriod.cp1.value)
    val fy2: Int = fallsInFinancialYear(accountingPeriod.cp2.value)

    val fy1Result = calculateForFinancialYear(fy1, b44, b38, b37, b39, accountingPeriod, constantsForTaxYear(TaxYear(fy1)))

    val fy2Result = if (fy2 != fy1) {
                      calculateForFinancialYear(fy2, b54, b38, b37, b39, accountingPeriod, constantsForTaxYear(TaxYear(fy2)))
                    } else BigDecimal(0)

    CATO04(roundedTwoDecimalPlaces(fy1Result + fy2Result))
  }

  private def calculateForFinancialYear(financialYear: Int,
                                        proRataProfitsChargeable: CtInteger,
                                        b38: B38,
                                        b37: B37,
                                        b39: B39,
                                        accountingPeriod: HmrcAccountingPeriod,
                                        constants: CtConstants): BigDecimal = {
    val daysInAccountingPeriod = daysBetween(accountingPeriod.cp1.value, accountingPeriod.cp2.value)

    val apDaysInFy = accountingPeriodDaysInFinancialYear(financialYear, accountingPeriod)

    val apFyRatio = apDaysInFy / daysInAccountingPeriod

    val msFyRatio = apDaysInFy / (365 max daysInAccountingPeriod)

    val apportionedProfit = (b37 plus b38) * apFyRatio

    val proRataLrma = (constants.lowerRelevantAmount * msFyRatio) / (b39.orZero + 1)

    val proRataUrma = constants.upperRelevantAmount * msFyRatio / (b39.orZero + 1)

    val mscrdueap = if (apportionedProfit > 0 &&
                        apportionedProfit > proRataLrma &&
                        apportionedProfit < (proRataUrma + BigDecimal("0.01"))) {
      (proRataUrma - apportionedProfit)*(proRataProfitsChargeable.value/apportionedProfit)* constants.reliefFraction
    } else {
      BigDecimal(0)
    }

    mscrdueap
  }

}
