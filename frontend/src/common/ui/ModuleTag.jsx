import { cx } from '../cx'
import Icon from '../icons/Icon'
import { MODULES } from '../modules'
import card from './FeedCard.module.css'

export default function ModuleTag({ module, children }) {
  return (
    <span className={cx(card.tag, `module-${module}`)}>
      <Icon name={MODULES[module].icon} size={16} />
      {children}
    </span>
  )
}
