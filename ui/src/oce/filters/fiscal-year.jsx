import React from "react";
import MultipleSelect from './inputs/multiple-select';

const FiscalYear = props => {
  return <MultipleSelect ep='/ocds/fiscalYear/all' {...props} />
}

export default FiscalYear;
