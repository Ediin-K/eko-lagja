export const MODULES = {
  notices: { path: '/notices', icon: 'campaign' },
  complaints: { path: '/complaints', icon: 'report' },
  polls: { path: '/polls', icon: 'how_to_vote' },
  help: { path: '/help', icon: 'volunteer_activism' },
}

export const MODULE_ORDER = ['notices', 'complaints', 'polls', 'help']

export const MENU_ITEMS = [
  { key: 'home', path: '/', icon: 'home', labelKey: 'menu.home', module: null },
  ...MODULE_ORDER.map((module) => ({
    key: module,
    path: MODULES[module].path,
    icon: MODULES[module].icon,
    labelKey: `modules.${module}`,
    module,
  })),
]

export function activeMenuIndex(pathname) {
  return MENU_ITEMS.findIndex((item) =>
    item.path === '/' ? pathname === '/' : pathname === item.path || pathname.startsWith(`${item.path}/`),
  )
}
