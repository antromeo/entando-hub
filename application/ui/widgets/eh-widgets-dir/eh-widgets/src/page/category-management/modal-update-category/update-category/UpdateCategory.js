import { Content, TextInput } from "carbon-components-react"
import { categorySchema } from "../../../../helpers/validation/categorySchema"
import i18n from "../../../../i18n"

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
  const changeCategory = (field, value) => {
    const newObj = {
      ...categoryObj,
    }
    newObj[field] = value
    onDataChange(newObj)
  }

  const onChangeHandler = (e, fieldName) => {
    changeCategory(fieldName, e.target.value)
  }

  return (
    <>
      <Content>
        <TextInput
          invalid={!!validationResult["name"]}
          invalidText={
            validationResult["name"] && validationResult["name"].join("; ")
          }
          id="name"
          value={categoryObj.name}
          labelText={`${i18n.t('component.bundleModalFields.name')} ${categorySchema.fields.name.exclusiveTests.required ? " *" : ""}`}
          onChange={(e) => onChangeHandler(e, "name")}
        />
        <TextInput
          invalid={!!validationResult["description"]}
          invalidText={
            validationResult["description"] &&
            validationResult["description"].join("; ")
          }
          id="description"
          value={categoryObj.description}
          labelText={`${i18n.t('component.bundleModalFields.description')} ${categorySchema.fields.description.exclusiveTests.required ? " *" : ""}`}
          onChange={(e) => onChangeHandler(e, "description")}
        />
      </Content>
    </>
  )
}

export default UpdateCategory
