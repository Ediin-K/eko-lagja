import { Route, Routes } from 'react-router-dom'
import AppLayout from './common/layout/AppLayout'
import PlaceholderPage from './common/layout/PlaceholderPage'
import NotificationsProvider from './common/notifications/NotificationsProvider'
import HomePage from './home/HomePage'

export default function App() {
  return (
    <NotificationsProvider>
      <Routes>
        <Route element={<AppLayout />}>
          <Route index element={<HomePage />} />
          <Route path="notices" element={<PlaceholderPage key="notices" titleKey="common:modules.notices" />} />
          <Route
            path="complaints"
            element={<PlaceholderPage key="complaints" titleKey="common:modules.complaints" />}
          />
          <Route
            path="complaints/new"
            element={<PlaceholderPage key="complaints-new" titleKey="home:report.title" />}
          />
          <Route path="polls" element={<PlaceholderPage key="polls" titleKey="common:modules.polls" />} />
          <Route path="help" element={<PlaceholderPage key="help" titleKey="common:modules.help" />} />
          <Route
            path="*"
            element={
              <PlaceholderPage
                key="not-found"
                titleKey="common:notFound.title"
                bodyKey="common:notFound.body"
                icon="error"
              />
            }
          />
        </Route>
      </Routes>
    </NotificationsProvider>
  )
}
