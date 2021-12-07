import { Content, TextInput } from "carbon-components-react"
import { useState } from "react";
import { categorySchema } from "../../../../helpers/validation/categorySchema"
import { CHAR_LENGTH, DESCRIPTION_MAX_LENGTH, LEAST_CHAR_NAME_MSG, MAX_CHAR_LENGTH, MAX_CHAR_LENGTH_FOR_DESC_CATEGORY_AND_ORG_FORM, MAX_CHAR_NAME_MSG, NAME_REQ_MSG } from "../../../../helpers/constants"
/*

Organisation:
{
    "name": "Entando inc.",
    "description": "Entando inc.",
    "bundleGroups": [],
    "organisationId": "1"
}

*/

const UpdateCategory = ({ categoryObj, onDataChange, validationResult }) => {
  const [categoryNameLength, setCategoryNameLength] = useState(false);
  const [catDescLength, setCatDescLength] = useState(false);
  const [isChanged, setIsChanged] = useState(false);

  const changeCategory = (field, value) => {
    const newObj = {
      ...categoryObj,
    }
    newObj[field] = value
    onDataChange(newObj)
  }

  const onChangeHandler = (e, fieldName) => {
    if (fieldName === 'description') {
      const msg = e.target.value.length > MAX_CHAR_LENGTH_FOR_DESC_CATEGORY_AND_ORG_FORM ? DESCRIPTION_MAX_LENGTH: ""
      validationResult["description"] = [msg]
    }

    if (fieldName === 'name') {
      !isChanged && setIsChanged(true)
      if (isChanged && e.target.value.length < CHAR_LENGTH) {
        const errorMessageForLengthZeroOrThree = e.target.value.length === 0 ? NAME_REQ_MSG : LEAST_CHAR_NAME_MSG
        validationResult["name"] = [errorMessageForLengthZeroOrThree]
      }
      if (isChanged && e.target.value.length > MAX_CHAR_LENGTH) {
        validationResult["name"] = [MAX_CHAR_NAME_MSG]
      }
    }
    fieldName === 'description' && setCatDescLength(e.target.value.length)
    fieldName === 'name' && setCategoryNameLength(e.target.value.length)
    changeCategory(fieldName, e.target.value)
  }

  return (
    <>
      <Content>
        <TextInput
          invalid={
            (isChanged && (categoryNameLength < CHAR_LENGTH || categoryNameLength > MAX_CHAR_LENGTH)) && !!validationResult["name"]
          }
          invalidText={
            (isChanged && (categoryNameLength < CHAR_LENGTH || categoryNameLength > MAX_CHAR_LENGTH)) ? (validationResult["name"] && validationResult["name"].join("; ")) : null
          }
          id="name"
          value={categoryObj.name}
          labelText={`Name ${categorySchema.fields.name.exclusiveTests.required ? " *" : ""}`}
          onChange={(e) => onChangeHandler(e, "name")}
          onBlur={(e) => console.log(e)}
        />
        <TextInput
          invalid={catDescLength > MAX_CHAR_LENGTH_FOR_DESC_CATEGORY_AND_ORG_FORM && !!validationResult["description"]}
          invalidText={
            catDescLength > MAX_CHAR_LENGTH_FOR_DESC_CATEGORY_AND_ORG_FORM && (
            validationResult["description"] &&
            validationResult["description"].join("; "))
          }
          id="description"
          value={categoryObj.description}
          labelText={`Description ${categorySchema.fields.description.exclusiveTests.required ? " *" : ""}`}
          onChange={(e) => onChangeHandler(e, "description")}
        />
      </Content>
    </>
  )
}

export default UpdateCategory
