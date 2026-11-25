import styles from './PageBand.module.css'

export default function PageBand({ children }) {
  return <div className={styles.band}>{children}</div>
}
